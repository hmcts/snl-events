package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;

@Repository
public interface StatusConfigRepository extends JpaRepository<StatusConfig, String> {
}
