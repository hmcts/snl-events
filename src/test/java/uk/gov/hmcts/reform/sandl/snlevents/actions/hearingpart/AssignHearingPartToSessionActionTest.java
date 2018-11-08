package uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart;

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
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
public class AssignHearingPartToSessionActionTest {
    private static final String SESSION_TYPE_CODE = "TYPE";
    private static final OffsetDateTime dateTime = OffsetDateTime.now();
    private static final OffsetDateTime START_TIME = dateTime.withHour(10).withMinute(0);

    private static final UUID ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440001");
    private static final UUID SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440002");
    private static final UUID SESSION_ID_PREVIOUS = UUID.fromString("123e4567-e89b-12d3-a456-426655440012");
    private static final UUID HEARING_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440003");
    private static final SessionType SESSION_TYPE = new SessionType(SESSION_TYPE_CODE, "");

    private AssignHearingPartToSessionAction action;
    private HearingPartSessionRelationship request;
    private Session mockedSession;

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;
    private final HearingPart mockedHearingPart = new HearingPart();

    @Before
    public void setup() {
        SessionAssignmentData sessionAssignmentData = new SessionAssignmentData();
        sessionAssignmentData.setSessionId(SESSION_ID);
        sessionAssignmentData.setSessionVersion(0);

        request = new HearingPartSessionRelationship();
        request.setHearingPartId(ID);
        request.setHearingPartVersion(0);
        request.setSessionData(sessionAssignmentData);
        request.setStart(START_TIME);
        request.setUserTransactionId(UUID.randomUUID());

        action = createAction(request);

        mockSessionRepositoryReturnsSession();
        mockHearingPart();
    }

    @Test
    public void act_shouldSetClassFieldsToProperState() throws JsonProcessingException {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearingPart.getVersion()).isEqualTo(0);
        assertThat(action.previousSession).isNull();
        assertThat(action.hearingPart.getVersion()).isEqualTo(request.getHearingPartVersion());
        assertThat(action.hearingPart.getSession()).isEqualTo(mockedSession);
        assertThat(action.hearingPart.getSessionId()).isEqualTo(mockedSession.getId());

        Mockito.verify(objectMapper, times(2)).writeValueAsString(any());
    }

    @Test(expected = SnlEventsException.class)
    public void act_shouldThrowServiceException_whenObjectMapperFailsToWriteJson() throws JsonProcessingException {
        action.getAndValidateEntities();
        Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();
        action.act();
        UUID[] ids = action.getAssociatedEntitiesIds();
        assertThat(ids).isEqualTo(new UUID[] {
            ID, HEARING_ID, SESSION_ID, SESSION_ID
        });
    }

    @Test
    public void generateFactMessages_shouldReturnOneElementWithTypeUpsertSession() {
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
            "update",
            "update",
            0)
        );
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            ID,
            action.previousHearingPart,
            "update",
            "update",
            1)
        );
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID));

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();
        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }


    @Test
    public void getUserTransactionData_returnsCorrectData_withPreviousSession() throws IOException {
        Session previousSession = createSession();
        previousSession.setId(SESSION_ID_PREVIOUS);
        mockedHearingPart.setSessionId(SESSION_ID_PREVIOUS);
        mockedHearingPart.setSession(previousSession);

        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> expectedTransactionData = new ArrayList<>();
        expectedTransactionData.add(new UserTransactionData("hearing",
            HEARING_ID,
            action.previousHearing,
            "update",
            "update",
            0)
        );
        expectedTransactionData.add(new UserTransactionData("hearingPart",
            ID,
            action.previousHearingPart,
            "update",
            "update",
            1)
        );
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID_PREVIOUS));
        expectedTransactionData.add(getLockedSessionTransactionData(SESSION_ID));

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();
        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private void mockHearingPart() {
        final Hearing hearing = new Hearing();
        hearing.setId(HEARING_ID);
        hearing.setDuration(Duration.ofMinutes(1));
        hearing.setCaseType(new CaseType("code", "desc"));
        hearing.setHearingType(new HearingType("code", "Desc"));
        hearing.setScheduleStart(dateTime);
        hearing.setScheduleEnd(dateTime.plusHours(1));
        hearing.setCreatedAt(dateTime);

        mockedHearingPart.setId(ID);
        mockedHearingPart.setVersion(0L);
        mockedHearingPart.setSessionId(null);
        mockedHearingPart.setSession(null);
        mockedHearingPart.setHearing(hearing);
        mockedHearingPart.setHearingId(hearing.getId());
        Mockito.when(hearingPartRepository.findOne(ID)).thenReturn(mockedHearingPart);
        Mockito.when(hearingPartRepository.save(mockedHearingPart)).thenReturn(mockedHearingPart);
    }

    private void mockSessionRepositoryReturnsSession() {
        mockedSession = createSession();
        Mockito.when(sessionRepository.findOne(SESSION_ID)).thenReturn(mockedSession);
    }

    private Session createSession() {
        Session s = new Session();
        s.setId(SESSION_ID);
        s.setStart(dateTime);
        return s;
    }

    private AssignHearingPartToSessionAction createAction(HearingPartSessionRelationship request) {
        return new AssignHearingPartToSessionAction(request.getHearingPartId(), request,
            hearingPartRepository, sessionRepository, entityManager, objectMapper);
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }
}
