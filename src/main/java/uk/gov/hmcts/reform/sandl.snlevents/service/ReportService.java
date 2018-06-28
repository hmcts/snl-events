package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.ListedHearingRequestReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private HearingPartRepository hearingQPartRepository;

    public List<UnlistedHearingRequestsReportResult> reportUnlistedHearingRequests() {
        return hearingQPartRepository.reportUnlistedHearingRequests();
    }

    public List<ListedHearingRequestReportResult> reportListedHearingRequests(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        return hearingQPartRepository.reportListedHearingRequests(fromDate, toDate);
    }
}
