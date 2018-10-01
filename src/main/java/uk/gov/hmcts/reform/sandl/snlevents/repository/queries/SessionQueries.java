package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

public final class SessionQueries {

    public static final String GET_SESSION_INFO_SQL =
        "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r, s.sessionTypeCode, s.version) "
        + "FROM Session s LEFT OUTER JOIN s.person as p LEFT OUTER JOIN s.room as r "
        + "WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND (p.personType = 'JUDGE' or p is null)";

    private SessionQueries() {
    }
}
