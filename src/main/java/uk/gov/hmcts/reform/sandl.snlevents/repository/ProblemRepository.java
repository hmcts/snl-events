package uk.gov.hmcts.reform.sandl.snlevents.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.Problem;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

}
