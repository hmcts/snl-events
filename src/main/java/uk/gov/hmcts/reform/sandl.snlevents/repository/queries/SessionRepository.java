package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository {
    String GET_SESSION_INFO_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.SessionInfo(s.id, s.start, s.duration, p, r) " +
        "FROM Session s, Person p, Room r WHERE s.start BETWEEN :dateStart AND :dateEnd " +
        "AND p.personType = 'JUDGE'";
}
