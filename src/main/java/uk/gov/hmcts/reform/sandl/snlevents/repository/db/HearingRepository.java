package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import java.util.UUID;

@Repository
public interface HearingRepository  extends JpaRepository<Hearing, UUID>, JpaSpecificationExecutor<Hearing> {
}
