package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository {
    String GET_SESSION_WITH_JUDGE_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.SessionWithJudge(s.id, s.start, s.duration, p) " +
        "FROM Session s, Person p WHERE s.start BETWEEN :dateStart AND :dateEnd AND s.person = p.id " +
        "AND p.personType = 'JUDGE'";
}
