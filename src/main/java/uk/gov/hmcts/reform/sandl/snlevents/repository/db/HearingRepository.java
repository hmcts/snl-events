package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;

import java.util.UUID;

@Repository
public interface HearingRepository extends JpaRepository<Hearing, UUID>, JpaSpecificationExecutor<Hearing> {

    String HEARING_FOR_LISTING_QUERY = "SELECT h.id, case_number, case_title, ct.code as case_type_code, "
        + "ct.description as case_type_description, ht.code as hearing_type_code, "
        + "ht.description as hearing_type_description, (duration * 1000000000) as duration, "
        + "schedule_start, schedule_end, version, "
        + "priority, communication_facilitator, reserved_judge_id, p.name as reserved_judge_name, number_of_sessions, "
        + "h.status, is_multisession "
        + "FROM hearing h "
        + "LEFT JOIN person p on p.id = h.reserved_judge_id "
        + "INNER JOIN case_type ct on h.case_type_code = ct.code "
        + "INNER JOIN hearing_type ht on h.hearing_type_code = ht.code "
        + "INNER JOIN status_config sc on h.status = sc.status WHERE can_be_listed = true AND h.status != 'Listed'";

    @Query(nativeQuery = true, value = "select * from hearing where id=:1")
    Hearing getHearingByIdIgnoringWhereDeletedClause(UUID id);
}
