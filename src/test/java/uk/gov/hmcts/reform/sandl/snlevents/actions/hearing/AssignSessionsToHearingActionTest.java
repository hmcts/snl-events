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
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
public class AssignSessionsToHearingActionTest {
    private static final OffsetDateTime dateTime = OffsetDateTime.now();
    private static final OffsetDateTime START_TIME = dateTime.withHour(10).withMinute(0);

    private static final UUID HEARING_PART_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440001");
    private static final UUID HEARING_PART_ID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426655440011");
    private static final UUID SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440002");
    private static final UUID SESSION_ID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426655440022");
    private static final UUID HEARING_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440003");

    private AssignSessionsToHearingAction action;
    private HearingSessionRelationship request;
    private HearingPart mockedHearingPart;
    private HearingPart mockedHearingPart2;
    private Session mockedSession;
    private Session mockedSession2;

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private HearingRepository hearingRepository;

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
        request.setUserTransactionId(UUID.randomUUID());

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
    public void getAndValidateEntities_forZeroHearingPartsAttachedToHearing_shouldThrowException() {
        Hearing mockedHearing = createHearing();
        mockedHearing.setHearingParts(Collections.emptyList());
        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(mockedHearing);

        action.getAndValidateEntities();
    }


    //need a check for single session
    @Test
    public void act_forMultiSessionAssignment_shouldSetClassFieldsToProperState() throws JsonProcessingException {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearing.getVersion()).isEqualTo(0);
        assertThat(action.hearing.getVersion()).isEqualTo(request.getHearingVersion());
        assertThat(action.targetSessions).isEqualTo(Arrays.asList(mockedSession, mockedSession2));
        assertThat(action.hearing.getHearingParts().size()).isEqualTo(2);
        assertThat(action.hearing.getHearingParts().get(0).getSession()).isEqualTo(mockedSession);
        assertThat(action.hearing.getHearingParts().get(0).getStart()).isEqualTo(mockedSession.getStart());
        assertThat(action.hearing.getHearingParts().get(1).getSession()).isEqualTo(mockedSession2);
        assertThat(action.hearing.getHearingParts().get(1).getStart()).isEqualTo(mockedSession2.getStart());

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
        expectedTransactionData.add(new UserTransactionData("hearing",
            HEARING_ID,
            action.previousHearing,
            "lock",
            "unlock",
            0)
        );
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            HEARING_PART_ID,
            action.previousHearingParts.get(0),
            "update",
            "update",
            1)
        );
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            HEARING_PART_ID_2,
            action.previousHearingParts.get(1),
            "update",
            "update",
            1)
        );
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID));
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID_2));

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();
        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }


    @Test
    public void getUserTransactionData_returnsCorrectData_withPreviousSession() {
        // currently not supported operation - To implement later
        assertThat(true).isEqualTo(true);
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
            hearingRepository, sessionRepository, entityManager, objectMapper);
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }
}
