package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import config.JpaTestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
public class SessionServiceTests {
    public static final long VERSION = 1L;
    @Mock
    EntityManager entityManager;

    @Before
    public void init() {
        EntityManager em = mock(EntityManager.class);
        sessionService.entityManager = em;

        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());
    }

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.MAX;
    private static final long DURATION = 1L;
    private static final String CASE_TYPE = "case-type";
    private static final String UUID_STRING = "38400000-8cf0-11bd-b23e-10b96e4ef00d";
    private static final String JUDGE_NAME = "judge-name";
    public static final LocalDate START_DATE = LocalDate.MIN;
    public static final LocalDate END_DATE = LocalDate.MAX;

    @TestConfiguration
    static class SessionServiceTestContextConfiguration {
        @Bean
        public SessionService sessionService() { return new SessionService(); }
    }

    @Autowired
    private SessionService sessionService;

    @MockBean
    private SessionRepository sessionRepository;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private PersonRepository personRepository;
    @MockBean
    private HearingPartRepository hearingPartRepository;
    @MockBean
    private UserTransactionService userTransactionService;
    @MockBean
    private ObjectMapper objectMapper;
    @MockBean
    private FactsMapper factsMapper;
    @MockBean
    private RulesService rulesService;

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
    //todo something wrong with mocking typed query
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
        List<HearingPart> hearingParts = createHearingParts();
        List<Session> sessions = createSessions(VERSION);

        when(sessionRepository.findSessionByStartDate(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(sessions);
        when(hearingPartRepository.findBySessionIn(any(List.class)))
            .thenReturn(hearingParts);

        SessionWithHearings sessionWithHearings = sessionService.getSessionsWithHearingsForDates(START_DATE, END_DATE);

        List<SessionInfo> sessionInfos = createSessionInfos();

        assertThat(sessionWithHearings.getHearingParts()).isEqualTo(hearingParts);
        assertThat(sessionWithHearings.getSessions()).isEqualTo(sessionInfos);
    }

    @Test
    public void getSessionJudgeDiaryForDates_returnsSessionsWithHearingsFromRepository() {
        List<HearingPart> hearingParts = createHearingParts();
        List<Session> sessions = createSessions(VERSION);

        when(sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(
            any(OffsetDateTime.class), any(OffsetDateTime.class), eq(JUDGE_NAME))
        )
            .thenReturn(sessions);
        when(hearingPartRepository.findBySessionIn(any(List.class)))
            .thenReturn(hearingParts);

        SessionWithHearings sessionWithHearings = sessionService.getSessionJudgeDiaryForDates(JUDGE_NAME, START_DATE, END_DATE);

        List<SessionInfo> sessionInfos = createSessionInfos();

        assertThat(sessionWithHearings.getHearingParts()).isEqualTo(hearingParts);
        assertThat(sessionWithHearings.getSessions()).isEqualTo(sessionInfos);
    }

    @Test
    public void save_savesSessionToRepository() {
        when(roomRepository.findOne(any(UUID.class))).thenReturn(getRoom());
        when(personRepository.findOne(any(UUID.class))).thenReturn(getPerson());

        Session savedSession = sessionService.save(createUpsertSession());
        verify(sessionRepository, times(1)).save(any(Session.class));
        assertThat(savedSession).isEqualToComparingFieldByFieldRecursively(createSession());
    }

    @Test
    public void saveWithTransaction_startsTransaction() {
        when(userTransactionService.startTransaction(eq(createUuid()), any(List.class)))
            .thenReturn(createUserTransaction());

        UserTransaction transaction = sessionService.saveWithTransaction(createUpsertSession());

        assertThat(transaction).isEqualToComparingFieldByFieldRecursively(createUserTransaction());
        verify(userTransactionService, times(1))
            .startTransaction(eq(createUuid()), eq(createUserTransactionDataList()));
    }

    @Test
    public void updateSession_updatesWithTransaction_whenTheresNoTransactionInProgress() throws IOException {
        Session session = createSession();
        String message = "message";

        when(userTransactionService.isAnyBeingTransacted(any(UUID.class)))
            .thenReturn(false);
        when(userTransactionService.startTransaction(any(UUID.class), any(List.class)))
            .thenReturn(createUserTransaction());
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(session);
        when(userTransactionService.rulesProcessed(any(UserTransaction.class))).then(returnsFirstArg());
        when(factsMapper.mapUpdateSessionToRuleJsonMessage(eq(session))).thenReturn(message);

        UserTransaction transaction = sessionService.updateSession(createUpsertSession());

        verify(rulesService, times(1)).postMessage(any(UUID.class), eq(RulesService.UPSERT_SESSION), eq(message));
        assertThat(transaction).isEqualToComparingFieldByFieldRecursively(createUserTransaction());
    }

    @Test
    public void updateSession_indicatesConflict_whenTransactionIsInProgress() throws IOException {
        when(userTransactionService.isAnyBeingTransacted(any(UUID.class)))
            .thenReturn(true);
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());

        UserTransaction ut = createUserTransaction(UserTransactionStatus.CONFLICT);
        when(userTransactionService.transactionConflicted(any(UUID.class)))
            .thenReturn(ut);

        UserTransaction transaction = sessionService.updateSession(createUpsertSession());

        assertThat(transaction).isEqualToComparingFieldByFieldRecursively(ut);
    }

    private UserTransaction createUserTransaction() {
        return createUserTransaction(null);
    }

    private UserTransaction createUserTransaction(UserTransactionStatus status) {
        UserTransaction transaction = new UserTransaction();
        transaction.setId(createUuid());
        transaction.setStatus(status);

        return transaction;
    }

    private Session createSession() {
        return createSession(null);
    }

    private Session createSession(Long version) {
        Session session = new Session();
        session.setCaseType(CASE_TYPE);
        session.setDuration(createDuration());
        session.setId(createUuid());
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

    private UUID createUuid() {
        return UUID.fromString(UUID_STRING);
    }

    private Duration createDuration() {
        return Duration.ofDays(DURATION);
    }

    private List<Session> createSessions(Long version) {
        return new ArrayList<>(Arrays.asList(createSession(version)));
    }

    private SessionInfo createSessionInfo() {
        SessionInfo sessionInfo = new SessionInfo(
            createUuid(),
            OFFSET_DATE_TIME,
            createDuration(),
            getPerson(),
            getRoom(),
            CASE_TYPE,
            VERSION
        );

        return sessionInfo;
    }

    private List<SessionInfo> createSessionInfos() {
        return new ArrayList<>(Arrays.asList(createSessionInfo()));
    }

    private HearingPart createHearingPart() {
        return new HearingPart();
    }

    private List createHearingParts() {
        return new ArrayList<>(Arrays.asList(createHearingPart()));
    }

    private UpsertSession createUpsertSession() {
        UpsertSession session = new UpsertSession();
        session.setCaseType(CASE_TYPE);
        session.setDuration(createDuration());
        session.setId(createUuid());
        session.setUserTransactionId(createUuid());
        session.setStart(OFFSET_DATE_TIME);
        session.setRoomId(UUID_STRING);
        session.setPersonId(UUID_STRING);
        session.setVersion(VERSION);

        return session;
    }

    private List<UserTransactionData> createUserTransactionDataList() {
        return new ArrayList<>(Arrays.asList(new UserTransactionData(
            "session",
            createUuid(),
            null,
            "insert",
            "delete",
            0)
        ));
    }
}
