package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository {

    String GET_SESSION_INFO_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r) "
        + "FROM Session s LEFT OUTER JOIN s.person as p LEFT OUTER JOIN s.room as r "
        + "WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND (p.personType = 'JUDGE' or p is null)";

    String GET_SESSION_FOR_JUDGE_DIARY_SQL = "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r) "
        + "FROM Session s LEFT OUTER JOIN s.person as p LEFT OUTER JOIN s.room as r "
        + "WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND p.personType = 'JUDGE' AND p.username = :judgeUsername";
}
