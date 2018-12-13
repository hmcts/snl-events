package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import java.util.UUID;

@Repository
public interface HearingRepository extends JpaRepository<Hearing, UUID>, JpaSpecificationExecutor<Hearing> {
    @Query(nativeQuery = true, value = "select * from hearing where id=:uuid")
    Hearing getHearingByIdIgnoringWhereDeletedClause(@Param("uuid") UUID uuid);
}
