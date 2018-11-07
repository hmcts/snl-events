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
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.StatusConfigResponse;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(StatusConfigController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class StatusConfigControllerTest {
    public static final String URL = "/status-config";

    @Autowired
    private EventsMockMvc mvc;

    @MockBean
    private StatusConfigService statusConfigService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;


    @Test
    public void fetchAllRooms_returnsRoomsFromService() throws Exception {
        val statuses = createStatusConfigs();
        when(statusConfigService.getStatusConfigs()).thenReturn(statuses);

        val response = mvc.getAndMapResponse(URL, new TypeReference<List<StatusConfigResponse>>() {
        });
        assertThat(response).isEqualTo(createStatusConfigResponses());
    }

    private List<StatusConfig> createStatusConfigs() {
        return Arrays.asList(new StatusConfig());
    }

    private List<StatusConfigResponse> createStatusConfigResponses() {
        return Arrays.asList(new StatusConfigResponse());
    }
}
