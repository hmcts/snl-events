package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.ListedHearingRequestReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private HearingPartRepository hearingQPartRepository;

    public List<UnlistedHearingRequestsReportResult> reportUnlistedHearingRequests() {
        return hearingQPartRepository.reportUnlistedHearingRequests();
    }

    public List<ListedHearingRequestReportResult> reportListedHearingRequests() {
        return hearingQPartRepository.reportListedHearingRequests();
    }
}
