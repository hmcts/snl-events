package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ProblemQueries.GET_PROBLEMS;

@Repository
public interface ProblemRepository extends PagingAndSortingRepository<Problem, String> {

    @Query(GET_PROBLEMS)
    Page<Problem> getAllSortedBySeverityAndCreatedAt(Pageable pageable);

    /**
     * Retruns all problems, it may be a lot of them.
     * @deprecated (This function has been replaced with getAllSortedBySeverityAndCreatedAt pagable)
     */
    @Query(GET_PROBLEMS)
    @Deprecated()
    List<Problem> getAllSortedBySeverityAndCreatedAt();

    @Query("SELECT problem "
        + "FROM Problem problem LEFT OUTER JOIN problem.references as pr "
        + "WHERE pr.entityId = :entity_id")
    List<Problem> getProblemsByReferenceEntityId(@Param("entity_id") String entityId);

    @Query("SELECT problem "
        + "FROM Problem problem "
        + "WHERE problem.userTransactionId = :user_transaction_id")
    List<Problem> getProblemsByUserTransactionId(@Param("user_transaction_id") UUID userTransactionId);
}
