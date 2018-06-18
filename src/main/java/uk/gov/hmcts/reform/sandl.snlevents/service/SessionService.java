package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_FOR_JUDGE_DIARY_SQL;
import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_INFO_SQL;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    private RulesService rulesService;

    @PersistenceContext
    EntityManager entityManager;

    public List getSessions() {
        return sessionRepository.findAll();
    }

    public Session getSessionById(UUID id) {
        return sessionRepository.findOne(id);
    }

    public List getSessionsFromDate(LocalDate localDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(localDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(localDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_INFO_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .getResultList();
    }

    public List<SessionInfo> getSessionsForDates(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_INFO_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .getResultList();
    }

    public List<SessionInfo> getJudgeDiaryForDates(String judgeUsername, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_FOR_JUDGE_DIARY_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .setParameter("judgeUsername", judgeUsername)
            .getResultList();
    }

    public SessionWithHearings getSessionJudgeDiaryForDates(String judgeUsername, LocalDate startDate,
                                                            LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(fromDate,
            toDate, judgeUsername);

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(sessions);

        SessionWithHearings sessionWithHearings = new SessionWithHearings();
        sessionWithHearings.setSessions(sessions);
        sessionWithHearings.setHearingParts(hearingParts);

        return sessionWithHearings;
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    public Session save(CreateSession createSession) {
        Session session = new Session();
        session.setId(createSession.getId());
        session.setDuration(createSession.getDuration());
        session.setStart(createSession.getStart());
        session.setCaseType(createSession.getCaseType());

        if (createSession.getRoomId() != null) {
            Room room = roomRepository.findOne(createSession.getRoomId());
            session.setRoom(room);
        }

        if (createSession.getPersonId() != null) {
            Person person = personRepository.findOne(createSession.getPersonId());
            session.setPerson(person);
        }

        return this.save(session);
    }

    @Transactional
    public UserTransaction saveWithTransaction(CreateSession createSession) {
        Session session = save(createSession);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("session", session.getId(), null, "insert", "delete", 0));

        return userTransactionService.startTransaction(createSession.getUserTransactionId(), userTransactionDataList);
    }

    public UserTransaction updateSession(CreateSession createSession) throws IOException {

        Session session = getSessionById(createSession.getId());

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(Arrays.asList(session));

        return areTransactionsInProgress(session, hearingParts)
            ? userTransactionService.transactionConflicted(createSession.getUserTransactionId())
            : updateWithTransaction(session, createSession, hearingParts);
    }

    private boolean areTransactionsInProgress(Session session, List<HearingPart> hearingParts) {
        List<UUID> entityIds = hearingParts.stream().map(HearingPart::getId).collect(Collectors.toList());
        entityIds.add(session.getId());

        return userTransactionService.isAnyBeingTransacted(entityIds.stream().toArray(UUID[]::new));
    }

    @Transactional
    public UserTransaction updateWithTransaction(Session session, CreateSession createSession, List<HearingPart> hearingParts) throws IOException {
        session = updateSessionTime(session, createSession);
        save(session);

        List<UserTransactionData> userTransactionDataList = generateUserTransactionDataList(session, hearingParts);

        UserTransaction ut = userTransactionService.startTransaction(createSession.getUserTransactionId(), userTransactionDataList);

        String msg = factsMapper.mapUpdateSessionToRuleJsonMessage(session);

        rulesService.postMessage(ut.getId(), RulesService.UPSERT_SESSION, msg);

        ut = userTransactionService.rulesProcessed(ut);

        return userTransactionService.commit(ut.getId());
    }

    private Session updateSessionTime(Session session, CreateSession createSession) {
        Optional.ofNullable(createSession.getDuration()).ifPresent((d) -> session.setDuration(d));
        Optional.ofNullable(createSession.getStart()).ifPresent((s) -> session.setStart(s));

        return session;
    }

    private List<UserTransactionData> generateUserTransactionDataList(Session session, List<HearingPart> hearingParts) throws JsonProcessingException {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.addAll(Arrays.asList(new UserTransactionData("session",
            session.getId(),
            objectMapper.writeValueAsString(session),
            "update",
            "update",
            0)
        ));
        hearingParts.forEach((hp) -> {

            String beforeData = null;
            try {
                beforeData = objectMapper.writeValueAsString(hp);
            } catch(JsonProcessingException e ) {
                throw new RuntimeException(e);
            }
            userTransactionDataList.add(new UserTransactionData("session",
                hp.getId(),
                beforeData,
                "lock",
                "unlock",
                0)
            );
        });
    }
}
