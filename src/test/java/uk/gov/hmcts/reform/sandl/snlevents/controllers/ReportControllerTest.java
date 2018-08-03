package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.ListedHearingRequestReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReportService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ReportController.class)
@Import(TestConfiguration.class)
public class ReportControllerTest {
    public static final String URL = "/report";

    @MockBean
    private ReportService reportService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SAuthenticationService s2SAuthenticationService;
    @Autowired
    private MockMvc mvc;

    @Before
    public void setupMock() {
        when(s2SAuthenticationService.validateToken(any())).thenReturn(true);
    }

    @Test
    public void getUnlistedHearingRequests_returnsResultsFromService() throws Exception {
        val results = createUnlistedHearingRequestReportResults();

        when(reportService.reportUnlistedHearingRequests())
            .thenReturn(results);

        getResponseAndExpectOk(URL + "/unlisted-hearing-requests");
    }

    private List<UnlistedHearingRequestsReportResult> createUnlistedHearingRequestReportResults() {
        return Arrays.asList(new UnlistedHearingRequestsReportResult() {
            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public int getHearings() {
                return 0;
            }

            @Override
            public int getMinutes() {
                return 0;
            }
        });
    }

    @Test
    public void getListedHearingRequests_returnsResultsFromService() throws Exception {
        val startDate = LocalDate.of(1,2,3);
        val endDate = LocalDate.of(4,5,6);

        val startDateString = "03-02-0001";
        val endDateString = "06-05-0004";

        val results = createListedHearingRequestReportResults();

        when(reportService.reportListedHearingRequests(eq(startDate), eq(endDate)))
            .thenReturn(results);

        getResponseAndExpectOk(
            URL + "/listed-hearing-requests?startDate=" + startDateString + "&endDate=" + endDateString
        );
    }

    private void getResponseAndExpectOk(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isOk());
    }

    private List<ListedHearingRequestReportResult> createListedHearingRequestReportResults() {
        return Arrays.asList(new ListedHearingRequestReportResult() {
            @Override
            public String getCaseId() {
                return null;
            }

            @Override
            public String getCaseName() {
                return null;
            }

            @Override
            public String getJudge() {
                return null;
            }

            @Override
            public String getHearingType() {
                return null;
            }

            @Override
            public String getCaseType() {
                return null;
            }

            @Override
            public Duration getDuration() {
                return null;
            }

            @Override
            public OffsetDateTime getStartTime() {
                return null;
            }

            @Override
            public String getRoom() {
                return null;
            }
        });
    }
}
