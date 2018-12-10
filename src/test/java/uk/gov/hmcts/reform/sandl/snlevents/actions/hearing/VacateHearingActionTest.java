package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.common.ActionTestHelper;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VacateHearingRequest;
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class VacateHearingActionTest {
    private static final UUID HEARING_ID_TO_BE_VACATED = UUID.randomUUID();
    private static final Long HEARING_VERSION_TO_BE_VACATED = 0L;
    private static final UUID HEARING_PART_ID_A = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_A = 1L;
    private static final UUID HEARING_PART_ID_B = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_B = 2L;
    private static final UUID HEARING_PART_ID_C = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_C = 3L;
    private static final UUID HEARING_PART_ID_D = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_D = 3L;
    private static final UUID SESSION_ID_A = UUID.randomUUID();
    private static final UUID SESSION_ID_B = UUID.randomUUID();
    private static final UUID SESSION_ID_C = UUID.randomUUID();
    private static final UUID SESSION_ID_D = UUID.randomUUID();
    private static StatusConfig hearingStatusConfig;
    private StatusesMock statusesMock = new StatusesMock();
    private VacateHearingAction action;

    private ActionTestHelper ath = new ActionTestHelper();

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
        hearingStatusConfig = statusesMock.statusConfigService.getStatusConfig(Status.Listed);
        Hearing hearing = new Hearing();
        hearing.setDuration(Duration.ofMinutes(60));
        hearing.setCaseType(new CaseType("cs-code", "cs-desc"));
        hearing.setHearingType(new HearingType("ht-code", "ht-desc"));
        hearing.setId(HEARING_ID_TO_BE_VACATED);
        hearing.setStatus(hearingStatusConfig);
        hearing.setVersion(HEARING_VERSION_TO_BE_VACATED);
        hearing.setHearingParts(Arrays.asList(
            ath.createHearingPartWithSession(HEARING_PART_ID_A, HEARING_VERSION_ID_A, hearing,
                Status.Listed, OffsetDateTime.now().plusDays(-1), SESSION_ID_A, OffsetDateTime.now().plusDays(-1)),
            ath.createHearingPartWithSession(HEARING_PART_ID_B, HEARING_VERSION_ID_B, hearing,
                Status.Listed, OffsetDateTime.now(), SESSION_ID_B, OffsetDateTime.now()),
            ath.createHearingPartWithSession(HEARING_PART_ID_C, HEARING_VERSION_ID_C, hearing,
                Status.Listed, OffsetDateTime.now().plusDays(1), SESSION_ID_C, OffsetDateTime.now().plusDays(1)),
            ath.createHearingPartWithSession(HEARING_PART_ID_D, HEARING_VERSION_ID_D, hearing,
                Status.Vacated, OffsetDateTime.now().plusDays(2), SESSION_ID_D, OffsetDateTime.now().plusDays(2))
        ));
        hearing.setMultiSession(true);

        when(hearingRepository.findOne(eq(HEARING_ID_TO_BE_VACATED))).thenReturn(hearing);

        VacateHearingRequest vhr = new VacateHearingRequest();
        vhr.setHearingId(HEARING_ID_TO_BE_VACATED);
        vhr.setUserTransactionId(UUID.randomUUID());

        action = new VacateHearingAction(
            vhr,
            hearingRepository,
            hearingPartRepository,
            statusesMock.statusConfigService,
            statusesMock.statusServiceManager,
            objectMapper,
            entityManager
        );
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        UUID[] expectedUuids = new UUID[]{
            HEARING_ID_TO_BE_VACATED,
            HEARING_PART_ID_C,
            SESSION_ID_C,
        };

        ath.assertThat_getAssociatedEntitiesIds_returnsCorrectIds(action, expectedUuids);
    }

    @Test
    public void getUserTransactionId_shouldReturnUuid() {
        assertNotNull(action.getUserTransactionId());
    }

    @Test
    public void generateUserTransactionData_shouldSetHearingPartSessionIdToNull() {
        action.getAndValidateEntities();
        action.act();
        List<UserTransactionData> userTransactionData = action.generateUserTransactionData();

        assertThatContainsEntityWithUpdateAction(userTransactionData, HEARING_PART_ID_C, "hearingPart");
        assertThatContainsEntityWithUpdateAction(userTransactionData, HEARING_ID_TO_BE_VACATED, "hearing");
        assertThatContainsEntity(userTransactionData, "session", SESSION_ID_C);
    }

    @Test
    public void generateFactMessages_shouldReturnMsgForUpdatedHearingParts() {
        action.getAndValidateEntities();
        val generatedFactMsgs = action.generateFactMessages();

        assertThat(generatedFactMsgs.size()).isEqualTo(1);
        assertThatAllMsgsAreTypeOf(generatedFactMsgs, RulesService.DELETE_HEARING_PART);
    }

    @Test
    public void act_shouldSetHearingPartSessionIdToNull() {
        ath.assertHearingPartsSessionIsSetToNull(action, hearingPartRepository);
    }

    @Test
    public void act_shouldNotChangeHearingStatuses() {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearing.getStatus().getStatus()).isEqualTo(hearingStatusConfig.getStatus());
    }

    @Test
    public void act_shouldSetStatusesToVacated() {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearingParts.get(0).getStatus().getStatus()).isEqualTo(Status.Vacated);
    }

    @Test(expected = SnlRuntimeException.class)
    public void act_whenObjectIsInvalid_shouldThrowSnlRuntimeException() throws JsonProcessingException {
        action.getAndValidateEntities();
        Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_whenHearingStatusCantBeVacated_shouldThrowException() {
        Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        hearing.setVersion(HEARING_VERSION_TO_BE_VACATED);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Vacated));
        hearing.getHearingParts().forEach(hp -> {
            hp.getSession().setHearingParts(Arrays.asList(hp));
            hp.setStart(OffsetDateTime.now());
        });


        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(hearing);
        action.getAndValidateEntities();
    }


    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_whenHearingIsSingleSessionHearing_shouldThrowException() {
        Hearing hearing = new Hearing();
        hearing.setVersion(HEARING_VERSION_TO_BE_VACATED);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(false);
        Mockito.when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearing);
        action.getAndValidateEntities();
    }

    private void assertThatContainsEntityWithUpdateAction(List<UserTransactionData> userTransactionData, UUID entityId,
                                                          String entityName) {
        val hearingUserTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == entityId)
            .findFirst().get();
        assertThat(hearingUserTransactionData.getEntity()).isEqualTo(entityName);
        assertThat(hearingUserTransactionData.getAction()).isEqualTo("update");
        assertThat(hearingUserTransactionData.getCounterAction()).isEqualTo("update");
    }

    private void assertThatContainsEntity(List<UserTransactionData> userTransactionData, String entity, UUID id) {
        val hearingUserTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == id)
            .findFirst().get();
        assertThat(hearingUserTransactionData.getEntity()).isEqualTo(entity);
        assertThat(hearingUserTransactionData.getAction()).isEqualTo("lock");
        assertThat(hearingUserTransactionData.getCounterAction()).isEqualTo("unlock");
    }

    private void assertThatAllMsgsAreTypeOf(List<FactMessage> factMessages, String type) {
        factMessages.forEach(fm -> assertThat(fm.getType()).isEqualTo(type));
    }
}
