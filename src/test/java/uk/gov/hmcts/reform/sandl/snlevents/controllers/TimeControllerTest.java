package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TimeController.class)
@Import(TestConfiguration.class)
public class TimeControllerTest {
    public static final String URL = "/time";

    @MockBean
    private RulesService rulesService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SAuthenticationService s2SAuthenticationService;

    @MockBean
    private FactsMapper factsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void upsert_postsTimeToRulesService() throws Exception {
        val time = createDateTimePartValue();
        val mappedTime = "time";

        when(factsMapper.mapTimeToRuleJsonMessage(eq(time))).thenReturn(mappedTime);

        val content = objectMapper.writeValueAsString(time);
        mvc.getMockMvc().perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isOk());

        verify(rulesService, times(1))
            .postMessage(eq("upsert-type"), eq(mappedTime));
    }

    private DateTimePartValue createDateTimePartValue() {
        return new DateTimePartValue("type", 1);
    }
}
