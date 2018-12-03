package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

public final class SessionQueries {

    public static final String GET_SESSION_INFO_SQL =
        "SELECT NEW uk.gov.hmcts.reform.sandl.snlevents.model.response."
        + "SessionInfo(s.id, s.start, s.duration, p, r, s.sessionTypeCode, s.version) "
        + "FROM Session s LEFT OUTER JOIN s.person as p LEFT OUTER JOIN s.room as r "
        + "WHERE s.start BETWEEN :dateStart AND :dateEnd "
        + "AND (p.personType = 'JUDGE' or p is null)";

    // duration is multiply by 1000000000 in order to convert it from nanoseconds to seconds
    public static final String GET_SESSION_AMEND_RESPONSE_SQL =
        "SELECT"
        + "  s.id                                   AS id, "
        + "  s.start                                AS start, "
        + "  s.duration * 1000000000                AS duration, "
        + "  s.session_type_code                    AS session_type_code, "
        + "  p.name                                 AS person_name, "
        + "  r.name                                 AS room_name, "
        + "  r.room_type_code                       AS room_type_code, "
        + "  rt.description                         AS room_description, "
        + "  (SELECT COUNT(hearing_part.id) FROM hearing_part "
        + "   WHERE hearing_part.session_id = s.id AND hearing_part.is_deleted = FALSE) AS hearing_parts_count, "
        + "  (SELECT CASE WHEN EXISTS("
        + "        SELECT h.id FROM hearing_part hp "
        + "    INNER JOIN hearing h on hp.hearing_id = h.id "
        + "    WHERE h.is_multisession = TRUE AND hp.session_id = s.id) "
        + "    THEN TRUE"
        + "    ELSE FALSE END)                      AS has_multi_session_hearing_assigned, "
        + "    s.version                            AS version"
        + " FROM Session s "
        + "  LEFT JOIN Room r on s.room_id = r.id "
        + "  LEFT JOIN person p on s.person_id = p.id "
        + "  LEFT JOIN room_type rt on r.room_type_code = rt.code "
        + "WHERE s.id = :session_id";

    private SessionQueries() {
    }
}
