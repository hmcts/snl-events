package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HearingPartRepository extends JpaRepository<HearingPart, UUID> {

    List<HearingPart> findBySessionIn(Collection<Session> sessions);

    @Query(nativeQuery = true,
        value =
            "select cast('Require listing in the next 4 weeks' as varchar(100)) as title, cast(count(hearing_part) as int) as hearings, cast(ceiling(sum(duration)/60) as int) as minutes\n"
            + "from hearing_part\n"
            + "where session_id is null and schedule_end < DATE_TRUNC('day', now() + interval '4 week' + interval '1 day')\n"
//            + "union all\n"
//            + "select cast('Require listing in the next 3 months' as varchar(100)), cast(count(hearing_part) as int), cast(ceiling(sum(duration)/60) as int)\n"
//            + "from hearing_part\n"
//            + "where session_id is null and schedule_end < DATE_TRUNC('day', now() + interval '3 month' + interval '1 day')\n"
//            + "union all\n"
//            + "select cast('Require listing beyond the next 3 months' as varchar(100)), cast(count(hearing_part) as int), cast(ceiling(sum(duration)/60) as int)\n"
//            + "from hearing_part\n"
//            + "where session_id is null and schedule_end > DATE_TRUNC('day', now() + interval '3 month' + interval '1 day')\n"
//            + "union all\n"
//            + "select cast('Target schedule not provided' as varchar(100)), cast(count(hearing_part) as int), cast(ceiling(sum(duration)/60) as int)\n"
//            + "from hearing_part\n"
//            + "where session_id is null and (schedule_start is null or schedule_end is null)"
    )
    List<UnlistedHearingRequestsReportResult> reportUnlistedHearingRequests();
}
