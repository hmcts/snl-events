package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_FOR_JUDGE_DIARY_SQL;
import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_INFO_SQL;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List getSessions() {
        return sessionRepository.findAll();
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

    public void save(Session session) {
        sessionRepository.save(session);
    }

}
