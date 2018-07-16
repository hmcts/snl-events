package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SearchController.class)
public class SearchControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RulesService rulesService;

    @SuppressWarnings("PMD.UnusedPrivateField")
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void searchPossibleSessions_returnsSessions() throws Exception {
        val params = "?from=from&to=to&durationInSeconds=1&judge=judge&room=room";
        val ret = "ret";
        //ensure rulesService is called with correct params
        when(rulesService.search(eq(params))).thenReturn(ret);

        val response = mvc
            .perform(get("/search" + params))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        val r = response.getContentAsString();

        assertThat(r).isEqualTo(ret);
    }

    //todo use dataProvider instead of copying test body
    @Test
    public void searchPossibleSessions_returnsSessions_whenJudgeAndRoomIsNotProvided() throws Exception {
        val params = "?from=from&to=to&durationInSeconds=1";
        val ret = "ret";
        //ensure rulesService is called with correct params
        when(rulesService.search(eq(params))).thenReturn(ret);

        val response = mvc
            .perform(get("/search" + params))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        val r = response.getContentAsString();

        assertThat(r).isEqualTo(ret);
    }
}
