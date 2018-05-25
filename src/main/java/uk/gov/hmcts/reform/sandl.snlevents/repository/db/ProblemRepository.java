package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {
    String FIND_PROBLEMS_BY_REFERENCE_TYPE_ID_SQL =
        "SELECT problem "
            + "FROM Problem problem LEFT OUTER JOIN problem.references as pr "
            + "WHERE pr.entityId = :entity_id";
}
