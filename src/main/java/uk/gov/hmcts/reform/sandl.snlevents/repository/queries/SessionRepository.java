package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository {
    String GET_SESSION_INFO_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r) "
        + "FROM Session s, Person p, Room r WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND p.personType = 'JUDGE'";

    String GET_SESSION_FOR_JUDGE_DIARY_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r) "
        + "FROM Session s, Person p, Room r WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND p.personType = 'JUDGE' AND p.username = :judgeUsername";
}
