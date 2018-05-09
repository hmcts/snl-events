package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;

@Repository
public interface HearingPartRepository extends JpaRepository<HearingPart, UUID> {

}
