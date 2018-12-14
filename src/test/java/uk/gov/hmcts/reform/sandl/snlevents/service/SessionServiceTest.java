package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.actions.session.DragAndDropSessionAction;
import uk.gov.hmcts.reform.sandl.snlevents.config.JpaTestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(JpaTestConfiguration.class)
public class SessionServiceTest {

    public static final long VERSION = 1L;
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.MAX;
    private static final long DURATION = 1L;
    private static final String SESSION_TYPE = "session-type";
    private static final String SESSION_TYPE_DESC = "session-type-desc";
    private static final String UUID_STRING = "38400000-8cf0-11bd-b23e-10b96e4ef00d";
    private static final String JUDGE_NAME = "judge-name";
    private static final String HEARING_ID = "7684d2a2-6bf2-4a20-a75a-c6593fcc7c63";
    public static final LocalDate START_DATE = LocalDate.MIN;
    public static final LocalDate END_DATE = LocalDate.MAX;
    public StatusesMock statusesMock = new StatusesMock();

    @InjectMocks
    private SessionService sessionService;

    @Mock
    EntityManager entityManager;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private HearingPartRepository hearingPartRepository;
    @Mock
    private SessionTypeRepository sessionTypeRepository;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FactsMapper factsMapper;
    @Mock
    private RulesService rulesService;
    @Mock
    private ActionService actionService;

    @Before
    public void init() {
        EntityManager em = mock(EntityManager.class);
        sessionService.entityManager = em;

        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());
    }

    @Test
    public void getSessions_returnsSessionsFromRepository() {
        List<Session> repositorySessions = createSessions(null);
        when(sessionRepository.findAll()).thenReturn(repositorySessions);

        List serviceSessions = sessionService.getSessions();

        assertThat(serviceSessions).isEqualTo(repositorySessions);
    }

    @Test
    public void getSessionById_returnsSessionFromRepository() {
        Session repositorySession = createSession();
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(repositorySession);

        Session serviceSession = sessionService.getSessionById(UUID.randomUUID());

        assertThat(serviceSession).isEqualTo(repositorySession);
    }

    //@Test
    //nottodo something wrong with mocking typed query
    public void getSessionsFromDate_returnsSessionsInfoFromRepository() {
        TypedQuery query = mock(TypedQuery.class);
        List<SessionInfo> repositorySessionInfos = createSessionInfos();
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(repositorySessionInfos);

        when(entityManager.createQuery(anyString(), eq(SessionInfo.class))).thenReturn(query);

        List serviceSessionInfos = sessionService.getSessionsFromDate(LocalDate.now());

        assertThat(serviceSessionInfos).isEqualTo(repositorySessionInfos);
    }

    @Test
    public void getSessionsWithHearingsForDates_returnsSessionsWithHearingsFromRepository() {
        List<HearingPart> hearingPart = createHearingParts();
        List<HearingPartResponse> hearingPartResponses = createHearingPartReponses();
        List<Session> sessions = createSessions(VERSION);

        when(sessionRepository.findSessionByStartDate(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(sessions);
        when(hearingPartRepository.findBySessionIn(any(List.class)))
            .thenReturn(hearingPart);

        SessionWithHearings sessionWithHearings = sessionService.getSessionsWithHearingsForDates(START_DATE, END_DATE);

        List<SessionInfo> sessionInfos = createSessionInfos();

        assertThat(sessionWithHearings.getHearingPartsResponse()).isEqualTo(hearingPartResponses);
        assertThat(sessionWithHearings.getSessions()).isEqualTo(sessionInfos);
    }

    @Test
    public void getSessionJudgeDiaryForDates_returnsSessionsWithHearingsFromRepository() {
        List<HearingPart> hearingPart = createHearingParts();
        List<HearingPartResponse> hearingPartReponses = createHearingPartReponses();
        List<Session> sessions = createSessions(VERSION);

        when(sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(
            any(OffsetDateTime.class), any(OffsetDateTime.class), eq(JUDGE_NAME))
        )
            .thenReturn(sessions);
        when(hearingPartRepository.findBySessionIn(any(List.class)))
            .thenReturn(hearingPart);

        SessionWithHearings sessionWithHearings = sessionService
            .getSessionJudgeDiaryForDates(JUDGE_NAME, START_DATE, END_DATE);

        List<SessionInfo> sessionInfos = createSessionInfos();

        assertThat(sessionWithHearings.getHearingPartsResponse()).isEqualTo(hearingPartReponses);
        assertThat(sessionWithHearings.getSessions()).isEqualTo(sessionInfos);
    }

    @Test
    public void save_savesSessionToRepository() {
        when(roomRepository.findOne(any(UUID.class))).thenReturn(getRoom());
        when(personRepository.findOne(any(UUID.class))).thenReturn(getPerson());
        when(sessionTypeRepository.findOne(any(String.class)))
            .thenReturn(new SessionType(SESSION_TYPE, SESSION_TYPE_DESC));

        Session savedSession = sessionService.save(createUpsertSession());
        verify(sessionRepository, times(1)).save(any(Session.class));
        assertThat(savedSession).isEqualToComparingFieldByFieldRecursively(createSession());
    }

    @Test
    public void saveWithTransaction_startsTransaction() {
        when(userTransactionService.startTransaction(eq(createUuid(UUID_STRING)), any(List.class)))
            .thenReturn(createUserTransaction());
        when(sessionTypeRepository.findOne(any(String.class))).thenReturn(new SessionType("code", "desc"));

        UserTransaction transaction = sessionService.saveWithTransaction(createUpsertSession());

        assertThat(transaction).isEqualToComparingFieldByFieldRecursively(createUserTransaction());
        verify(userTransactionService, times(1))
            .startTransaction(eq(createUuid(UUID_STRING)), eq(createUserTransactionDataList()));
    }

    @Test
    public void updateSession_callsDragAndDropSessionAction() {
        sessionService.updateSession(new DragAndDropSessionRequest());
        verify(actionService, times(1)).execute(any(DragAndDropSessionAction.class));
    }

    private UserTransaction createUserTransaction() {
        return createUserTransaction(null);
    }

    private UserTransaction createUserTransaction(UserTransactionStatus status) {
        UserTransaction transaction = new UserTransaction();
        transaction.setId(createUuid(UUID_STRING));
        transaction.setStatus(status);

        return transaction;
    }

    private Session createSession() {
        return createSession(null);
    }

    private Session createSession(Long version) {
        Session session = new Session();
        session.setSessionType(new SessionType(SESSION_TYPE, SESSION_TYPE_DESC));
        session.setDuration(createDuration());
        session.setId(createUuid(UUID_STRING));
        session.setPerson(getPerson());
        session.setRoom(getRoom());
        session.setStart(OFFSET_DATE_TIME);
        session.setVersion(version);

        return session;
    }

    private Room getRoom() {
        return new Room();
    }

    private Person getPerson() {
        return new Person();
    }

    private UUID createUuid(String id) {
        return UUID.fromString(id);
    }

    private Duration createDuration() {
        return Duration.ofDays(DURATION);
    }

    private List<Session> createSessions(Long version) {
        return Arrays.asList(createSession(version));
    }

    private SessionInfo createSessionInfo() {
        return new SessionInfo(
            createUuid(UUID_STRING),
            OFFSET_DATE_TIME,
            createDuration(),
            getPerson(),
            getRoom(),
            SESSION_TYPE,
            VERSION
        );
    }

    private List<SessionInfo> createSessionInfos() {
        return Arrays.asList(createSessionInfo());
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        val h = createHearing();
        hp.setHearing(h);
        hp.setHearingId(h.getId());
        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Unlisted);
        hp.setStatus(statusConfig);

        return hp;
    }

    private List<HearingPartResponse> createHearingPartReponses() {
        return createHearingParts().stream().map(hp -> new HearingPartResponse(hp)).collect(Collectors.toList());
    }

    private List<HearingPart> createHearingParts() {
        return Arrays.asList(createHearingPart());
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(createUuid(HEARING_ID));
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        return hearing;
    }

    private UpsertSession createUpsertSession() {
        UpsertSession session = new UpsertSession();
        session.setSessionType(SESSION_TYPE);
        session.setDuration(createDuration());
        session.setId(createUuid(UUID_STRING));
        session.setUserTransactionId(createUuid(UUID_STRING));
        session.setStart(OFFSET_DATE_TIME);
        session.setRoomId(UUID_STRING);
        session.setPersonId(UUID_STRING);
        session.setVersion(VERSION);

        return session;
    }

    private List<UserTransactionData> createUserTransactionDataList() {
        return Arrays.asList(new UserTransactionData(
            "session",
            createUuid(UUID_STRING),
            null,
            "insert",
            "delete",
            0)
        );
    }
}
