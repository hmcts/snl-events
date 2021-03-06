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
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.activities.ActivityStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
public class AssignSessionsToHearingActionTest {
    private static final OffsetDateTime dateTime = OffsetDateTime.now();
    private static final OffsetDateTime START_TIME = dateTime.withHour(10).withMinute(0);

    private static final UUID TRANSACTION_ID = UUID.randomUUID();
    private static final UUID HEARING_PART_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440001");
    private static final UUID HEARING_PART_ID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426655440011");
    private static final UUID SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440002");
    private static final UUID SESSION_ID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426655440022");
    private static final UUID HEARING_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440003");
    private static final String UPDATE_ACTION_TEXT = "update";

    private AssignSessionsToHearingAction action;
    private HearingSessionRelationship request;
    private HearingPart mockedHearingPart;
    private HearingPart mockedHearingPart2;
    private Session mockedSession;
    private Session mockedSession2;
    private StatusesMock statusesMock = new StatusesMock();

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        SessionAssignmentData sessionAssignmentData = new SessionAssignmentData();
        sessionAssignmentData.setSessionId(SESSION_ID);
        sessionAssignmentData.setSessionVersion(0);
        SessionAssignmentData sessionAssignmentData2 = new SessionAssignmentData();
        sessionAssignmentData2.setSessionId(SESSION_ID_2);
        sessionAssignmentData2.setSessionVersion(0);

        request = new HearingSessionRelationship();
        request.setHearingId(HEARING_PART_ID);
        request.setHearingVersion(0);
        request.setSessionsData(Arrays.asList(sessionAssignmentData, sessionAssignmentData2));
        request.setStart(START_TIME);
        request.setUserTransactionId(TRANSACTION_ID);

        action = createAction(request);

        mockSessionRepositoryReturnsSession();
        mockHearing();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forEmptySessionData_shouldThrowException() {
        action.relationship.setSessionsData(null);

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forWrongTargetSessionData_shouldThrowException() {
        action.relationship.setSessionsData(Collections.emptyList());
        Mockito.when(sessionRepository.findSessionByIdIn(anyListOf(UUID.class)))
            .thenReturn(Collections.emptyList());

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forLowerNumberOfSessionsInDbThenRequested_shouldThrowException() {
        action.relationship.setSessionsData(Collections.singletonList(
            new SessionAssignmentData(UUID.randomUUID(), 0)
        ));
        Mockito.when(sessionRepository.findSessionByIdIn(anyListOf(UUID.class)))
            .thenReturn(Collections.emptyList());

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forZeroHearingPartsAttachedToHearing_shouldThrowException() {
        Hearing mockedHearing = createHearing();
        mockedHearing.setHearingParts(Collections.emptyList());
        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(mockedHearing);

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forNotListableHearing_shouldThrowException() {
        Hearing mockedHearing = createHearing();
        mockedHearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(mockedHearing);

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_forNotListableHearingPart_shouldThrowException() {
        Hearing mockedHearing = createHearing();
        final StatusConfig customStatusConfig = statusesMock.statusConfigService.getStatusConfig(Status.Listed);
        customStatusConfig.setCanBeListed(false);
        mockedHearing.setStatus(customStatusConfig);

        HearingPart hearingPart = createHearingPart(mockedHearing);
        hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        mockedHearing.setHearingParts(Collections.singletonList(hearingPart));

        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(mockedHearing);

        action.getAndValidateEntities();
    }

    @Test
    public void act_forMultiSessionAssignment_shouldSetClassFieldsToProperState() throws JsonProcessingException {
        action.getAndValidateEntities();
        action.act();

        final StatusConfig listedConfig = statusesMock.statusConfigService.getStatusConfig(Status.Listed);

        assertThat(action.hearing.getVersion()).isEqualTo(0);
        assertThat(action.hearing.getVersion()).isEqualTo(request.getHearingVersion());
        assertThat(action.hearing.getStatus()).isEqualTo(listedConfig);
        assertThat(action.targetSessions).isEqualTo(Arrays.asList(mockedSession, mockedSession2));
        assertThat(action.hearing.getHearingParts().size()).isEqualTo(2);
        assertThat(action.hearing.getHearingParts().get(0).getSession()).isEqualTo(mockedSession);
        assertThat(action.hearing.getHearingParts().get(0).getStart()).isEqualTo(mockedSession.getStart());
        assertThat(action.hearing.getHearingParts().get(0).getStatus()).isEqualTo(listedConfig);

        assertThat(action.hearing.getHearingParts().get(1).getSession()).isEqualTo(mockedSession2);
        assertThat(action.hearing.getHearingParts().get(1).getStart()).isEqualTo(mockedSession2.getStart());
        assertThat(action.hearing.getHearingParts().get(1).getStatus()).isEqualTo(listedConfig);

        Mockito.verify(objectMapper, times(3)).writeValueAsString(any());
    }

    @Test
    public void act_forSingleSessionAssignment_shouldSetClassFieldsToProperState() throws JsonProcessingException {
        setupSingleSession();

        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearing.getVersion()).isEqualTo(0);
        assertThat(action.hearing.getVersion()).isEqualTo(request.getHearingVersion());
        assertThat(action.targetSessions).isEqualTo(Arrays.asList(mockedSession));
        assertThat(action.hearing.getHearingParts().size()).isEqualTo(1);
        assertThat(action.hearing.getHearingParts().get(0).getSession()).isEqualTo(mockedSession);
        assertThat(action.hearing.getHearingParts().get(0).getStart()).isEqualTo(request.getStart());
        Mockito.verify(objectMapper, times(2)).writeValueAsString(any());
    }

    private void setupSingleSession() {
        final Hearing hearing = createHearing();
        mockedHearingPart = createHearingPart(hearing);
        hearing.setHearingParts(Arrays.asList(mockedHearingPart));
        Mockito.when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearing);

        mockedSession = createSession();
        Mockito.when(sessionRepository.findSessionByIdIn(anyListOf(UUID.class)))
            .thenReturn(Arrays.asList(mockedSession));

        request.setSessionsData(Collections.singletonList(
            new SessionAssignmentData(SESSION_ID, 0)
        ));
    }

    @Test(expected = SnlRuntimeException.class)
    public void act_shouldThrowServiceException_whenObjectMapperFailsToWriteJson() throws JsonProcessingException {
        action.getAndValidateEntities();
        Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();


        UUID[] ids = action.getAssociatedEntitiesIds();
        assertThat(ids).isEqualTo(new UUID[] {
            HEARING_ID, HEARING_PART_ID, null, HEARING_PART_ID_2, null, SESSION_ID, SESSION_ID_2
        });

        action.act();
        ids = action.getAssociatedEntitiesIds();
        assertThat(ids).isEqualTo(new UUID[] {
            HEARING_ID, HEARING_PART_ID, SESSION_ID, HEARING_PART_ID_2, SESSION_ID_2, SESSION_ID, SESSION_ID_2
        });
    }

    @Test
    public void act_shouldSetHearingPart_to_Listed() {
        action.getAndValidateEntities();
        action.act();
        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
        captor.getValue().forEach(hp -> {
            assertNotNull(hp.getSessionId());
            assertNotNull(hp.getSession());
            assertThat(hp.getStatus().getStatus()).isEqualTo(Status.Listed);
        });
    }

    @Test
    public void generateFactMessages_forMultiSession_shouldReturnManyElementsWithTypeUpsertSession() {
        action.getAndValidateEntities();
        action.act();
        val factMsgs = action.generateFactMessages();

        assertThat(factMsgs.size()).isEqualTo(2);
        assertThat(factMsgs.get(0).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMsgs.get(0).getData()).isNotNull();
        assertThat(factMsgs.get(1).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMsgs.get(1).getData()).isNotNull();
    }

    @Test
    public void generateFactMessages_forSingleSession_shouldReturnOneElementWithTypeUpsertSession() {
        setupSingleSession();

        action.getAndValidateEntities();
        action.act();
        val factMsgs = action.generateFactMessages();

        assertThat(factMsgs.size()).isEqualTo(1);
        assertThat(factMsgs.get(0).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMsgs.get(0).getData()).isNotNull();
    }

    @Test
    public void getUserTransactionData_returnsCorrectData_whenNoPreviousSession() {
        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> expectedTransactionData = new ArrayList<>();
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            HEARING_PART_ID,
            action.originalHearingParts.get(0),
            UPDATE_ACTION_TEXT,
            UPDATE_ACTION_TEXT,
            2)
        );
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            HEARING_PART_ID_2,
            action.originalHearingParts.get(1),
            UPDATE_ACTION_TEXT,
            UPDATE_ACTION_TEXT,
            2)
        );
        expectedTransactionData.add(new UserTransactionData("hearing",
            HEARING_ID,
            null,
            "lock",
            "unlock",
            0)
        );
        expectedTransactionData.add(new UserTransactionData("hearing",
            HEARING_ID,
            action.previousHearing,
            UPDATE_ACTION_TEXT,
            UPDATE_ACTION_TEXT,
            1)
        );

        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID));
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID_2));

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();
        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    @Test
    public void getActivities_shouldProduceProperActivities() {
        action.getAndValidateEntities();

        List<ActivityLog> activities = action.getActivities();

        assertThat(activities.size()).isEqualTo(1);

        ActivityLog activityLog = activities.get(0);

        assertThat(activityLog.getStatus()).isEqualTo(ActivityStatus.Listed);
        assertThat(activityLog.getEntityName()).isEqualTo(Hearing.ENTITY_NAME);
        assertThat(activityLog.getUserTransactionId()).isEqualTo(TRANSACTION_ID);
    }

    //
    // Helpers
    //
    private void mockHearing() {
        final Hearing hearing = createHearing();

        mockedHearingPart = createHearingPart(hearing);
        mockedHearingPart2 = createHearingPart(hearing);
        mockedHearingPart2.setId(HEARING_PART_ID_2);
        hearing.setHearingParts(Arrays.asList(mockedHearingPart, mockedHearingPart2));

        Mockito.when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearing);
        Mockito.when(hearingRepository.save(hearing)).thenReturn(hearing);
    }

    private Hearing createHearing() {
        final Hearing hearing = new Hearing();
        hearing.setId(HEARING_ID);
        hearing.setDuration(Duration.ofMinutes(1));
        hearing.setCaseType(new CaseType("code", "desc"));
        hearing.setHearingType(new HearingType("code", "Desc"));
        hearing.setScheduleStart(dateTime);
        hearing.setScheduleEnd(dateTime.plusHours(1));
        hearing.setCreatedAt(dateTime);
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
        return hearing;
    }

    private HearingPart createHearingPart(Hearing hearing) {
        HearingPart hp = new HearingPart();
        hp.setId(HEARING_PART_ID);
        hp.setVersion(0L);
        hp.setSessionId(null);
        hp.setSession(null);
        hp.setHearing(hearing);
        hp.setHearingId(hearing.getId());
        hp.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
        return hp;
    }

    private void mockSessionRepositoryReturnsSession() {
        mockedSession = createSession();
        mockedSession2 = createSession();
        mockedSession2.setId(SESSION_ID_2);
        Mockito.when(sessionRepository.findSessionByIdIn(anyListOf(UUID.class)))
            .thenReturn(Arrays.asList(mockedSession, mockedSession2));
    }

    private Session createSession() {
        Session s = new Session();
        s.setId(SESSION_ID);
        s.setStart(dateTime);
        return s;
    }

    private AssignSessionsToHearingAction createAction(HearingSessionRelationship request) {
        return new AssignSessionsToHearingAction(request.getHearingId(), request,
            hearingRepository, hearingPartRepository, sessionRepository, statusesMock.statusConfigService,
            statusesMock.statusServiceManager, entityManager, objectMapper
        );
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }
}
