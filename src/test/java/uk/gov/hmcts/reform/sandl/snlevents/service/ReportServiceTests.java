package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.report.UnlistedHearingRequestsReportResult;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ReportServiceTests {

    @InjectMocks
    ReportService reportService;

    @Mock
    HearingPartRepository hearingPartRepository;

    @Test
    public void reportUnlistedHearingRequests_getsRReportFromRepository() {
        val repositoryResult = createUnlistedHearingReportResults();

        when(hearingPartRepository.reportUnlistedHearingRequests())
            .thenReturn(repositoryResult);

        val serviceResult = reportService.reportUnlistedHearingRequests();

        assertThat(serviceResult).isEqualTo(repositoryResult);
    }

    private List<UnlistedHearingRequestsReportResult> createUnlistedHearingReportResults() {
        val result = mock(UnlistedHearingRequestsReportResult.class);

        return new ArrayList<>(Arrays.asList(result));
    }
}
