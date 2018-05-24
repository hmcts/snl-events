package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

public final class ProblemQueries {

    public static final String FIND_PROBLEMS_BY_REFERENCE_TYPE_ID_SQL =
        "SELECT problem "
        + "FROM Problem problem LEFT OUTER JOIN problem.references as pr "
        + "WHERE pr.typeId = :type_id";

    private ProblemQueries() {
    }
}
