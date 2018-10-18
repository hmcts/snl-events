package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

public final class ProblemQueries {

    public static final String GET_PROBLEMS = "SELECT problem FROM Problem problem "
        + "ORDER BY CASE severity "
        + "         WHEN 'Critical'  THEN '0' "
        + "         WHEN 'Urgent' THEN '1' "
        + "         WHEN 'Warning'  THEN '2' "
        + "         ELSE '2' "
        + "         END, created_at desc, id desc";

    private ProblemQueries() {
    }
}
