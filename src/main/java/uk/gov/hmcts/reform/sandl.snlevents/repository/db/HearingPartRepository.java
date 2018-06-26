package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.ListedHearingRequestReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HearingPartRepository extends JpaRepository<HearingPart, UUID> {

    List<HearingPart> findBySessionIn(Collection<Session> sessions);

    @Query(nativeQuery = true,
        value = "select cast(title as varchar(100)) as title, cast(hearings as int) as hearings, \n"
            + "cast(minutes as int) as minutes \n"
            + "from (\n"
            + "select 1 as sorted, 'Require listing in the next 4 weeks' as title, \n"
            + "count(hearing_part) as hearings, \n"
            + "coalesce(ceiling(sum(duration)/60), 0) as minutes\n"
            + "from hearing_part\n"
            + "where session_id is null \n"
            + "and schedule_end < DATE_TRUNC('day', now() + interval '4 week' + interval '1 day')\n"
            + "union all\n"
            + "select 2 as sorted, 'Require listing in the next 3 months' as title, \n"
            + "count(hearing_part) as hearings, \n"
            + "coalesce(ceiling(sum(duration)/60), 0) as minutes\n"
            + "from hearing_part\n"
            + "where session_id is null \n"
            + "and schedule_end >= DATE_TRUNC('day', now() + interval '4 week' + interval '1 day')\n"
            + "and schedule_end < DATE_TRUNC('day', now() + interval '3 month' + interval '1 day')\n"
            + "union all\n"
            + "select 3 as sorted, 'Require listing beyond the next 3 months' as title, \n"
            + "count(hearing_part) as hearings, \n"
            + "coalesce(ceiling(sum(duration)/60), 0) as minutes\n"
            + "from hearing_part\n"
            + "where session_id is null \n"
            + "and schedule_end >= DATE_TRUNC('day', now() + interval '3 month' + interval '1 day')\n"
            + "union all\n"
            + "select 4 as sorted, 'Target schedule not provided' as title, \n"
            + "count(hearing_part) as hearings, \n"
            + "coalesce(ceiling(sum(duration)/60), 0) as minutes\n"
            + "from hearing_part\n"
            + "where session_id is null \n"
            + "and (schedule_end is null)\n"
            + ") a order by sorted"

    )
    List<UnlistedHearingRequestsReportResult> reportUnlistedHearingRequests();

    @Query(value = "select hp.caseType as caseType, s.start as startTime, hp.caseNumber "
        + "as caseId, hp.caseTitle as caseName, hp.duration as duration, r.name as room, "
        + "p.name as judge, hp.hearingType as hearingType from HearingPart hp JOIN hp.session as s "
        + "LEFT OUTER JOIN s.room as r LEFT OUTER JOIN s.person as p WHERE s.start BETWEEN ?1 AND ?2")
    List<ListedHearingRequestReportResult> reportListedHearingRequests(OffsetDateTime startDate, OffsetDateTime endDate);
}
