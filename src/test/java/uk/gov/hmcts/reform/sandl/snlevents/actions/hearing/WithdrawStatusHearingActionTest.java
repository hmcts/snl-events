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
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.BaseStatusHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class WithdrawStatusHearingActionTest {
    private static final UUID HEARING_ID_TO_BE_WITHDRAWN = UUID.randomUUID();
    private static final UUID HEARING_PART_ID_A = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_A = 1L;
    private static final UUID HEARING_PART_ID_B = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_B = 2L;
    private static final UUID SESSION_ID_A = UUID.randomUUID();
    private static final UUID SESSION_ID_B = UUID.randomUUID();
    private static final List<VersionInfo> hearingVersions = Arrays.asList(
        getVersionInfo(HEARING_PART_ID_A, HEARING_VERSION_ID_A),
        getVersionInfo(HEARING_PART_ID_B, HEARING_VERSION_ID_B)
    );
    private StatusesMock statusesMock = new StatusesMock();
    private WithdrawStatusHearingAction action;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private ObjectMapper objectMapper;


    @Before
    public void setup() {
        Hearing hearing = new Hearing();
        hearing.setDuration(Duration.ofMinutes(60));
        hearing.setCaseType(new CaseType("cs-code", "cs-desc"));
        hearing.setHearingType(new HearingType("ht-code", "ht-desc"));
        hearing.setId(HEARING_ID_TO_BE_WITHDRAWN);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setHearingParts(Arrays.asList(
            createHearingPartWithSession(HEARING_PART_ID_A, HEARING_VERSION_ID_A,
                hearing, SESSION_ID_A, Status.Listed),
            createHearingPartWithSession(HEARING_PART_ID_B, HEARING_VERSION_ID_B,
                hearing, SESSION_ID_B, Status.Unlisted)
        ));

        when(hearingRepository.findOne(eq(HEARING_ID_TO_BE_WITHDRAWN))).thenReturn(hearing);

        BaseStatusHearingRequest bhr = new BaseStatusHearingRequest();
        bhr.setHearingId(HEARING_ID_TO_BE_WITHDRAWN);
        bhr.setUserTransactionId(UUID.randomUUID());

        bhr.setHearingPartsVersions(hearingVersions);

        action = new WithdrawStatusHearingAction(
            bhr, hearingRepository, hearingPartRepository,
            statusesMock.statusConfigService,
            statusesMock.statusServiceManager,
            objectMapper
        );
    }

    private HearingPart createHearingPartWithSession(UUID id, Long version, Hearing hearing, UUID sessionId,
                                                     Status status) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(id);
        hearingPart.setHearing(hearing);
        hearingPart.setVersion(version);
        hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(status));

        Session session = new Session();
        session.setId(sessionId);
        session.setStart(OffsetDateTime.now().plusDays(1));

        hearingPart.setSessionId(sessionId);
        hearingPart.setSession(session);

        return hearingPart;
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();
        UUID[] expectedUuids = new UUID[]{
            HEARING_ID_TO_BE_WITHDRAWN,
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
        assertNotNull(action.getUserTransactionId());
    }

    @Test
    public void generateFactMessages_shouldReturnMsgForUpdatedHearingParts() {
        action.getAndValidateEntities();
        val generatedFactMsgs = action.generateFactMessages();

        assertThat(generatedFactMsgs.size()).isEqualTo(2);
        assertThatAllMsgsAreTypeOf(generatedFactMsgs, RulesService.DELETE_HEARING_PART);
    }

    @Test
    public void act_shouldSetStatusesToWithdrawn() {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearing.getStatus().getStatus()).isEqualTo(Status.Withdrawn);
        assertThat(action.hearingParts.size()).isEqualTo(2);
        action.hearingParts.forEach(hearingPart -> {
            if (hearingPart.getId() == HEARING_PART_ID_A) {
                assertThat(hearingPart.getStatus().getStatus()).isEqualTo(Status.Vacated);
            } else if (hearingPart.getId() == HEARING_PART_ID_B) {
                assertThat(hearingPart.getStatus().getStatus()).isEqualTo(Status.Withdrawn);
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
    public void getAndValidateEntities_whenHearingStatusCantBeWithdrawn_shouldThrowException() {
        Hearing hearing = new Hearing();
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Adjourned));
        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(hearing);
        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_whenHearingPartsStatusCantBeWithdrawn_shouldThrowException() {
        Hearing hearing = new Hearing();
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setHearingParts(Arrays.asList(
            createHearingPartWithSession(HEARING_PART_ID_A, HEARING_VERSION_ID_A,
                hearing, SESSION_ID_A, Status.Adjourned),
            createHearingPartWithSession(HEARING_PART_ID_B, HEARING_VERSION_ID_B,
                hearing, SESSION_ID_B, Status.Adjourned)
        ));

        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(hearing);
        action.getAndValidateEntities();
    }

    private void assertThatAllMsgsAreTypeOf(List<FactMessage> factMessages, String type) {
        factMessages.stream().forEach(fm -> assertThat(fm.getType()).isEqualTo(type));
    }

    private static VersionInfo getVersionInfo(UUID id, Long version) {
        VersionInfo vi = new VersionInfo();
        vi.setId(id);
        vi.setVersion(version);

        return vi;
    }
}
