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

@Repository
public interface ProblemRepository extends PagingAndSortingRepository<Problem, String> {
    String getProblems = "SELECT problem FROM Problem problem "
        + "ORDER BY CASE severity "
        + "         WHEN 'Critical'  THEN '0' "
        + "         WHEN 'Urgent' THEN '1' "
        + "         WHEN 'Warning'  THEN '2' "
        + "         ELSE '2' "
        + "         END, created_at desc";

    @Query(getProblems)
    Page<Problem> getAllSortedBySeverityAndCreatedAt(Pageable pageable);

    @Query(getProblems)
    @Deprecated
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
