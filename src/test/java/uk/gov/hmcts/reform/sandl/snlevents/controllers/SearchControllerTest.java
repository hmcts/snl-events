package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SearchController.class)
public class SearchControllerTest {
    public static final String URL = "/search";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RulesService rulesService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SAuthenticationService s2SAuthenticationService;

    @Before
    public void setupMock() {
        when(s2SAuthenticationService.validateToken(any())).thenReturn(true);
    }

    @Test
    public void dataProvider_searchPossibleSessions_returnsSessions() throws Exception {
        searchPossibleSessions_returnsSessions("?from=from&to=to&durationInSeconds=1&judge=judge&room=room");
        searchPossibleSessions_returnsSessions("?from=from&to=to&durationInSeconds=1");
    }

    private void searchPossibleSessions_returnsSessions(String params) throws Exception {
        val ret = "ret";
        //ensure rulesService is called with correct params
        when(rulesService.search(eq(params))).thenReturn(ret);

        val response = mvc
            .perform(get(URL + params))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        val r = response.getContentAsString();

        assertThat(r).isEqualTo(ret);
    }
}