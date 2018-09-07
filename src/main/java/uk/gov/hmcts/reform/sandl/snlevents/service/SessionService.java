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
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_INFO_SQL;

@Service
public class SessionService {

    public static final String SESSION_ENTITY_NAME = "session";
    private final Function<Session, SessionInfo> sessionDbToSessionInfo =
        (Session s) -> {
            String sessionTypeCode = s.getSessionType() != null ? s.getSessionType().getCode() : null;
            return new SessionInfo(
                s.getId(),
                s.getStart(),
                s.getDuration(),
                s.getPerson(),
                s.getRoom(),
                sessionTypeCode,
                s.getCaseType(),
                s.getVersion()
            );
        };

    @PersistenceContext
    EntityManager entityManager;
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
    @Autowired
    private SessionTypeRepository sessionTypeRepository;

    public List getSessions() {
        return sessionRepository.findAll();
    }

    public Session getSessionById(UUID id) {
        return sessionRepository.findOne(id);
    }

    public SessionInfo getSessionInfoById(UUID id) {
        return sessionDbToSessionInfo.apply(sessionRepository.findOne(id));
    }

    //nottodo move entity manager to repository
    public List getSessionsFromDate(LocalDate localDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(localDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(localDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_INFO_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .getResultList();
    }

    //nottodo check if we can do this matching on repository level
    public SessionWithHearings getSessionsWithHearingsForDates(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartDate(fromDate, toDate);

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(sessions);

        SessionWithHearings sessionsWithHearings = new SessionWithHearings();
        sessionsWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionsWithHearings.setHearingParts(hearingParts);

        return sessionsWithHearings;
    }

    public SessionWithHearings getSessionJudgeDiaryForDates(String judgeUsername, LocalDate startDate,
                                                            LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(fromDate,
            toDate, judgeUsername);

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(sessions);

        SessionWithHearings sessionWithHearings = new SessionWithHearings();
        sessionWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionWithHearings.setHearingParts(hearingParts);

        return sessionWithHearings;
    }

    private Session save(Session session) {
        return sessionRepository.save(session);
    }

    public Session save(UpsertSession upsertSession) {
        Session session = new Session();
        session.setId(upsertSession.getId());
        session.setDuration(upsertSession.getDuration());
        session.setStart(upsertSession.getStart());
        session.setCaseType(upsertSession.getCaseType());

        if (upsertSession.getRoomId() != null && !upsertSession.getRoomId().isEmpty()) {
            Room room = roomRepository.findOne(getUuidFromString(upsertSession.getRoomId()));
            session.setRoom(room);
        }

        if (upsertSession.getPersonId() != null && !upsertSession.getPersonId().isEmpty()) {
            Person person = personRepository.findOne(getUuidFromString(upsertSession.getPersonId()));
            session.setPerson(person);
        }

        if (upsertSession.getSessionTypeCode() != null && !upsertSession.getSessionTypeCode().isEmpty()) {
            SessionType sessionType = sessionTypeRepository.findOne(upsertSession.getSessionTypeCode());
            session.setSessionType(sessionType);
        }

        return this.save(session);
    }

    @Transactional
    public UserTransaction saveWithTransaction(UpsertSession upsertSession) {
        Session session = save(upsertSession);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData(
            SESSION_ENTITY_NAME,
            session.getId(),
            null,
            "insert",
            "delete",
            0)
        );

        return userTransactionService.startTransaction(upsertSession.getUserTransactionId(), userTransactionDataList);
    }

    public UserTransaction updateSession(UpsertSession upsertSession) throws IOException {

        Session session = getSessionById(upsertSession.getId());
        entityManager.detach(session);
        session.setVersion(upsertSession.getVersion());

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(Arrays.asList(session));

        return areTransactionsInProgress(session, hearingParts)
            ? userTransactionService.transactionConflicted(upsertSession.getUserTransactionId())
            : updateWithTransaction(session, upsertSession, hearingParts);
    }

    private Session updateSession(Session session, UpsertSession upsertSession) {
        Optional.ofNullable(upsertSession.getDuration()).ifPresent(session::setDuration);
        Optional.ofNullable(upsertSession.getStart()).ifPresent(session::setStart);
        Optional.ofNullable(upsertSession.getCaseType()).ifPresent(session::setCaseType);

        setResources(session, upsertSession);

        return session;
    }

    private boolean areTransactionsInProgress(Session session, List<HearingPart> hearingParts) {
        List<UUID> entityIds = hearingParts.stream().map(HearingPart::getId).collect(Collectors.toList());
        entityIds.add(session.getId());

        return userTransactionService.isAnyBeingTransacted(entityIds.stream().toArray(UUID[]::new));
    }

    @Transactional
    public UserTransaction updateWithTransaction(Session givenSession,
                                                 UpsertSession upsertSession,
                                                 List<HearingPart> hearingParts) throws IOException {
        Session session = updateSession(givenSession, upsertSession);
        save(session);

        List<UserTransactionData> userTransactionDataList = generateUserTransactionDataList(session, hearingParts);

        UserTransaction ut = userTransactionService.startTransaction(upsertSession.getUserTransactionId(),
            userTransactionDataList);

        String msg = factsMapper.mapUpdateSessionToRuleJsonMessage(session);

        rulesService.postMessage(ut.getId(), RulesService.UPSERT_SESSION, msg);

        ut = userTransactionService.rulesProcessed(ut);

        return ut;
    }

    private void setResources(Session session, UpsertSession upsertSession) {
        Optional.ofNullable(upsertSession.getRoomId()).ifPresent(id -> {
            UUID roomId = getUuidFromString(upsertSession.getRoomId());
            Room room = (roomId == null) ? null : roomRepository.findOne(roomId);
            session.setRoom(room);
        });
        Optional.ofNullable(upsertSession.getPersonId()).ifPresent(id -> {
            UUID personId = getUuidFromString(upsertSession.getPersonId());
            Person person = (personId == null) ? null : personRepository.findOne(personId);
            session.setPerson(person);
        });
    }

    private UUID getUuidFromString(String id) {
        return "empty".equals(id) ? null : UUID.fromString(id);
    }

    private List<UserTransactionData> generateUserTransactionDataList(Session session, List<HearingPart> hearingParts)
        throws JsonProcessingException {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.addAll(Arrays.asList(new UserTransactionData(SESSION_ENTITY_NAME,
            session.getId(),
            objectMapper.writeValueAsString(session),
            "update",
            "update",
            0)
        ));
        for (HearingPart hp : hearingParts) {
            UserTransactionData transactionData = new UserTransactionData(SESSION_ENTITY_NAME,//NOPMD
                hp.getId(),
                objectMapper.writeValueAsString(hp),
                "lock",
                "unlock",
                0
            );
            userTransactionDataList.add(transactionData);
        }

        return userTransactionDataList;
    }
}
