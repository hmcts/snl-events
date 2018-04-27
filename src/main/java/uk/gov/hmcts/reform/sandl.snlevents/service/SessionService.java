package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sandl.snlevents.model.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionRepository.GET_SESSION_INFO_SQL;

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

        List<SessionInfo> list = entityManager.createQuery(GET_SESSION_INFO_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .getResultList();

        return list;
    }

    public void save(Session session) {
        sessionRepository.save(session);
    }
}
