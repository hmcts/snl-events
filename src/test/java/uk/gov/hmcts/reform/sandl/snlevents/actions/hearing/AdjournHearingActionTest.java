package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AdjournHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AdjournHearingActionTest {
    private static final UUID HEARING_ID_TO_BE_ADJOURNED = UUID.randomUUID();
    private static final Long HEARING_VERSION_TO_BE_ADJOURNED = 0L;
    private static final UUID HEARING_PART_ID_A = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_A = 1L;
    private static final UUID HEARING_PART_ID_B = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_B = 2L;
    private static final UUID SESSION_ID_A = UUID.randomUUID();
    private static final UUID SESSION_ID_B = UUID.randomUUID();
    private static final UUID TRANSACTION_ID = UUID.randomUUID();
    private StatusesMock statusesMock = new StatusesMock();
    private AdjournHearingAction action;

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private HearingPartRepository hearingPartRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EntityManager entityManager;

    @Before
    public void setup() {
        Hearing hearing = new Hearing();
        hearing.setDuration(Duration.ofMinutes(60));
        hearing.setCaseType(new CaseType("cs-code", "cs-desc"));
        hearing.setHearingType(new HearingType("ht-code", "ht-desc"));
        hearing.setId(HEARING_ID_TO_BE_ADJOURNED);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setVersion(HEARING_VERSION_TO_BE_ADJOURNED);
        hearing.setHearingParts(Arrays.asList(
            createHearingPartWithSession(HEARING_PART_ID_A, HEARING_VERSION_ID_A,
                hearing, SESSION_ID_A, Status.Listed, OffsetDateTime.now().minusDays(1)),
            createHearingPartWithSession(HEARING_PART_ID_B, HEARING_VERSION_ID_B,
                hearing, SESSION_ID_B, Status.Unlisted, OffsetDateTime.now().plusDays(1))
        ));
        when(hearingRepository.findOne(eq(HEARING_ID_TO_BE_ADJOURNED))).thenReturn(hearing);
        AdjournHearingRequest adjournHearingRequest = new AdjournHearingRequest();
        adjournHearingRequest.setHearingId(HEARING_ID_TO_BE_ADJOURNED);
        adjournHearingRequest.setUserTransactionId(TRANSACTION_ID);
        action = new AdjournHearingAction(
            adjournHearingRequest, hearingRepository, hearingPartRepository,
            statusesMock.statusConfigService,
            statusesMock.statusServiceManager,
            objectMapper,
            entityManager
        );
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();
        UUID[] expectedUuids = new UUID[]{
            HEARING_ID_TO_BE_ADJOURNED,
            HEARING_PART_ID_A,
            HEARING_PART_ID_B,
            SESSION_ID_A,
            SESSION_ID_B
        };
        assertThat(ids.length).isEqualTo(expectedUuids.length);
        assertTrue(Arrays.asList(ids).containsAll(Arrays.asList(expectedUuids)));
    }

    @Test
    public void getUserTransactionId_shouldReturnUuid() {
        assertThat(action.getUserTransactionId()).isEqualTo(TRANSACTION_ID);
    }

    @Test
    public void generateUserTransactionData_shouldSetProperEntities() {
        action.getAndValidateEntities();
        action.act();
        List<UserTransactionData> userTransactionData = action.generateUserTransactionData();
        assertThatContainsEntityWithUpdateAction(userTransactionData, HEARING_PART_ID_A, "hearingPart");
        assertThatContainsEntityWithUpdateAction(userTransactionData, HEARING_PART_ID_B, "hearingPart");
        assertThatContainsEntityWithUpdateAction(userTransactionData, HEARING_ID_TO_BE_ADJOURNED, "hearing");
        assertThatContainsLockedEntity(userTransactionData, "session", SESSION_ID_A);
        assertThatContainsLockedEntity(userTransactionData, "session", SESSION_ID_B);
    }

    @Test
    public void generateFactMessages_shouldReturnDeleteMessages() {
        action.getAndValidateEntities();
        val generatedFactMsgs = action.generateFactMessages();
        assertThat(generatedFactMsgs.size()).isEqualTo(2);
        assertThatAllMsgsAreTypeOf(generatedFactMsgs, RulesService.DELETE_HEARING_PART);
    }

    @Test
    public void act_shouldSetHearingPartSessionIdToNull() {
        action.getAndValidateEntities();
        action.act();
        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
        captor.getValue().forEach(hp -> {
            if (hp.getStatus().getStatus().equals(Status.Vacated)) {
                assertNull(hp.getSessionId());
                assertNull(hp.getSession());
            }
        });
    }

    @Test
    public void act_shouldSetProperStatuses() {
        action.getAndValidateEntities();
        action.act();
        assertThat(action.hearing.getStatus().getStatus()).isEqualTo(Status.Adjourned);
        assertThat(action.hearingParts.size()).isEqualTo(2);
        action.hearingParts.forEach(hearingPart -> {
            if (hearingPart.getId() == HEARING_PART_ID_A) {
                assertThat(hearingPart.getStatus().getStatus()).isEqualTo(Status.Listed);
            } else if (hearingPart.getId() == HEARING_PART_ID_B) {
                assertThat(hearingPart.getStatus().getStatus()).isEqualTo(Status.Vacated);
            }
        });
    }

    @Test(expected = SnlRuntimeException.class)
    public void act_whenObjectIsInvalid_shouldThrowSnlRuntimeException() throws JsonProcessingException {
        action.getAndValidateEntities();
        Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_whenHearingStatusCantBeAdjourned_shouldThrowException() {
        Hearing hearing = new Hearing();
        hearing.setVersion(HEARING_VERSION_TO_BE_ADJOURNED);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Adjourned));
        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(hearing);
        action.getAndValidateEntities();
    }

    private HearingPart createHearingPartWithSession(UUID id, Long version, Hearing hearing, UUID sessionId,
                                                     Status status, OffsetDateTime start) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(id);
        hearingPart.setHearing(hearing);
        hearingPart.setVersion(version);
        hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(status));
        Session session = new Session();
        session.setId(sessionId);
        session.setStart(start);
        hearingPart.setSessionId(sessionId);
        hearingPart.setSession(session);
        hearingPart.setStart(start);
        return hearingPart;
    }

    private void assertThatContainsEntityWithUpdateAction(List<UserTransactionData> userTransactionData, UUID entityId,
                                                          String entityName) {
        val hearingUserTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == entityId)
            .findFirst().get();
        assertThat(hearingUserTransactionData.getEntity()).isEqualTo(entityName);
        assertThat(hearingUserTransactionData.getAction()).isEqualTo("update");
        assertThat(hearingUserTransactionData.getCounterAction()).isEqualTo("update");
    }

    private void assertThatContainsLockedEntity(List<UserTransactionData> userTransactionData, String entity, UUID id) {
        val hearingUserTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == id)
            .findFirst().get();
        assertThat(hearingUserTransactionData.getEntity()).isEqualTo(entity);
        assertThat(hearingUserTransactionData.getAction()).isEqualTo("lock");
        assertThat(hearingUserTransactionData.getCounterAction()).isEqualTo("unlock");
    }

    private void assertThatAllMsgsAreTypeOf(List<FactMessage> factMessages, String type) {
        factMessages.stream().forEach(fm -> assertThat(fm.getType()).isEqualTo(type));
    }
}
