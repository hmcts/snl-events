package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ActivityResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ActivityLogRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(ActivityLogController.class)
@Import({TestConfiguration.class})
@AutoConfigureMockMvc(secure = false)
public class ActivityLogControllerTest {

    private static final String URL = "/activity-log";

    @Autowired
    private EventsMockMvc mvc;

    @MockBean
    private ActivityLogRepository activityLogRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Test
    public void getActivityLog_shouldReturnActivityResponseInRightOrder() throws Exception {
        val entityId = UUID.randomUUID();
        val activityLogs = new ArrayList<ActivityLog>();
        val dateTime = OffsetDateTime.now();

        val log1 = ActivityLog.builder()
            .createdAt(dateTime)
            .build();

        val log2 = ActivityLog.builder()
            .createdAt(dateTime.plusHours(1))
            .build();

        activityLogs.add(log2);
        activityLogs.add(log1);

        when(activityLogRepository.getActivityLogByEntityIdOrderByCreatedAtAsc(entityId)).thenReturn(activityLogs);

        val response = mvc.getAndMapResponse(URL + "/" + entityId, new TypeReference<List<ActivityResponse>>() {
        });

        assertThat(response.size()).isEqualTo(2);
    }
}
