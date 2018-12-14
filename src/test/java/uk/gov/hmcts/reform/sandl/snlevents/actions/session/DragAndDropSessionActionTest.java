package uk.gov.hmcts.reform.sandl.snlevents.actions.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DragAndDropSessionActionTest {
    private static final UUID SESSION_ID = UUID.randomUUID();
    private DragAndDropSessionAction action;
    private DragAndDropSessionRequest request;
    private HearingPart hearingPart;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        val start = OffsetDateTime.now();
        val duration = Duration.ofMinutes(30L);
        request = DragAndDropSessionRequest.builder()
            .sessionId(SESSION_ID)
            .start(start)
            .durationInSeconds(duration.getSeconds())
            .personId(UUID.randomUUID())
            .roomId(UUID.randomUUID())
            .userTransactionId(UUID.randomUUID())
            .version(1L)
            .build();

        action = new DragAndDropSessionAction(
            request,
            sessionRepository,
            roomRepository,
            personRepository,
            entityManager,
            objectMapper
        );

        Session session = new Session();
        session.setId(SESSION_ID);
        session.setStart(start);
        session.setDuration(duration);

        val hearing = createHearing();
        hearing.setMultiSession(false);

        hearingPart = createHearingPart(hearing);
        session.addHearingPart(hearingPart);

        when(sessionRepository.findOne(SESSION_ID)).thenReturn(session);
    }

    @Test(expected = SnlRuntimeException.class)
    public void getAndValidateEntities_ThrowsExceptionWhenTryToSetDifferentJudgeToSessionWithMultiSessionHearingPart() {
        Person judgeA = new Person();
        judgeA.setId(UUID.randomUUID());

        Session session = new Session();
        session.setId(SESSION_ID);
        session.setPerson(judgeA);

        val hearing = createHearing();
        hearing.setMultiSession(true);

        val hearingPart = createHearingPart(hearing);
        session.addHearingPart(hearingPart);

        request.setPersonId(UUID.randomUUID());

        when(sessionRepository.findOne(SESSION_ID)).thenReturn(session);

        action.getAndValidateEntities();
    }

    @Test(expected = SnlEventsException.class)
    public void act_whenObjectIsInvalid_shouldThrowSnlRuntimeException() throws JsonProcessingException {
        action.getAndValidateEntities();
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test
    public void act_savesUpdatedSession() {
        // Arrange
        Duration expectedDuration = Duration.ofMinutes(45);
        request.setDurationInSeconds(expectedDuration.getSeconds());

        OffsetDateTime expectedStartTime = OffsetDateTime.now().minusHours(10);
        request.setStart(expectedStartTime);

        UUID expectedRoomId = UUID.randomUUID();
        request.setRoomId(expectedRoomId);
        Room room = new Room();
        room.setId(expectedRoomId);
        when(roomRepository.findOne(expectedRoomId)).thenReturn(room);

        UUID expectedPersonId = UUID.randomUUID();
        request.setPersonId(expectedPersonId);
        Person person = new Person();
        person.setId(expectedPersonId);
        when(personRepository.findOne(expectedPersonId)).thenReturn(person);

        Session session = new Session();
        session.setId(SESSION_ID);
        when(sessionRepository.findOne(SESSION_ID)).thenReturn(session);

        // Act
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());

        Session savedSession = captor.getValue();

        assertThat(savedSession.getDuration()).isEqualTo(expectedDuration);
        assertThat(savedSession.getStart()).isEqualTo(expectedStartTime);
        assertThat(savedSession.getRoom().getId()).isEqualTo(expectedRoomId);
        assertThat(savedSession.getPerson().getId()).isEqualTo(expectedPersonId);
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids).isEqualTo(new UUID[]{SESSION_ID, hearingPart.getId()});
    }

    @Test
    public void generateFactMessages_shouldReturnOneElementWithTypeUpsertSession() {
        action.getAndValidateEntities();
        val factMsgs = action.generateFactMessages();

        assertThat(factMsgs.size()).isEqualTo(2);
        assertTrue(factMsgs.stream().anyMatch(fm -> fm.getType().equals(RulesService.UPSERT_HEARING_PART)));
        assertThat(factMsgs.stream().anyMatch(fm -> fm.getType().equals(RulesService.UPSERT_SESSION)));
    }

    @Test(expected = SnlEventsException.class)
    public void act_shouldThrowServiceException() throws JsonProcessingException {
        action.getAndValidateEntities();
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test
    public void generateUserTransactionData_returnsCorrectData() {
        action.getAndValidateEntities();
        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData.size()).isEqualTo(2);
        assertTrue(actualTransactionData.stream().anyMatch(utd -> utd.getEntity().equals("session")));
        assertTrue(actualTransactionData.stream().anyMatch(utd -> utd.getEntity().equals("hearingPart")));
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());
        return hearing;
    }

    private HearingPart createHearingPart(Hearing hearing) {
        val hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearing(hearing);
        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Listed);
        hearingPart.setStatus(statusConfig);
        return hearingPart;
    }
}
