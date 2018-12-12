package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.session.DragAndDropSessionAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionAmendResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SearchSessionQuery;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SearchSessionSelectColumn;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_AMEND_RESPONSE_SQL;
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
    private SessionTypeRepository sessionTypeRepository;
    @Autowired
    private UserTransactionService userTransactionService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SearchSessionQuery searchSessionQuery;
    @Autowired
    private ActionService actionService;

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

        List<HearingPartResponse> hearingPartsResponse = hearingPartRepository.findBySessionIn(sessions)
            .stream()
            .map(HearingPartResponse::new)
            .collect(Collectors.toList());

        SessionWithHearings sessionsWithHearings = new SessionWithHearings();
        sessionsWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionsWithHearings.setHearingPartsResponse(hearingPartsResponse);

        return sessionsWithHearings;
    }

    public SessionWithHearings getSessionJudgeDiaryForDates(String judgeUsername, LocalDate startDate,
                                                            LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(fromDate,
            toDate, judgeUsername);

        List<HearingPartResponse> hearingPartsResponse = hearingPartRepository.findBySessionIn(sessions)
            .stream()
            .map(HearingPartResponse::new)
            .collect(Collectors.toList());

        SessionWithHearings sessionWithHearings = new SessionWithHearings();
        sessionWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionWithHearings.setHearingPartsResponse(hearingPartsResponse);

        return sessionWithHearings;
    }

    public Session save(UpsertSession upsertSession) {
        Session session = new Session();
        session.setId(upsertSession.getId());
        session.setDuration(upsertSession.getDuration());
        session.setStart(upsertSession.getStart());
        if (upsertSession.getSessionType() != null && !upsertSession.getSessionType().isEmpty()) {
            val sessionType = sessionTypeRepository.findOne(upsertSession.getSessionType());
            session.setSessionType(sessionType);
        }

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

        return sessionRepository.save(session);
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

    public UserTransaction updateSession(DragAndDropSessionRequest dropSessionRequest) {
        DragAndDropSessionAction action = new DragAndDropSessionAction(
            dropSessionRequest,
            sessionRepository,
            roomRepository,
            personRepository,
            entityManager,
            objectMapper
        );

        return actionService.execute(action);
    }

    public Page<SessionSearchResponse> searchForSession(List<SearchCriteria> searchCriteriaList,
                                                        Pageable pageable,
                                                        SearchSessionSelectColumn orderByColumn,
                                                        Sort.Direction direction) {
        return searchSessionQuery.search(searchCriteriaList, pageable, orderByColumn, direction);
    }

    private UUID getUuidFromString(String id) {
        return "empty".equals(id) ? null : UUID.fromString(id);
    }

    public SessionAmendResponse getAmendSession(UUID id) {
        Query query = entityManager.createNativeQuery(GET_SESSION_AMEND_RESPONSE_SQL, "MapToSessionAmendResponse");
        query.setParameter("session_id", id);

        return (SessionAmendResponse) query.getSingleResult();
    }
}
