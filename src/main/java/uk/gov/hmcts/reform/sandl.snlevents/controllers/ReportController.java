package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.ListedHearingRequestReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ReportRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReportService;

import javax.persistence.Tuple;
import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(path = "unlisted-hearing-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UnlistedHearingRequestsReportResult> getUnlistedHearingRequests() {
        return reportService.reportUnlistedHearingRequests();
    }

    @GetMapping(path = "listed-hearing-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ListedHearingRequestReportResult> getListedHearingRequests() {
        return reportService.reportListedHearingRequests();
    }

}
