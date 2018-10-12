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
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.ProblemService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(ProblemController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class ProblemControllerTest {
    public static final String URL = "/problems";

    @MockBean
    private ProblemService problemService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void getProblems_returnsProblemsFromService() throws Exception {
        val problems = createProblems();
        when(problemService.getProblems(null)).thenReturn(createProblems());

        val response = mvc.getAndMapResponse(
            URL, new TypeReference<List<ProblemResponse>>(){}
        );

        assertThat(response).isEqualTo(problems);
    }

    @Test
    public void getProblemsByReferenceEntityId_returnsProblemsFromService() throws Exception {
        val id = UUID.randomUUID().toString();
        val problems = createProblems();
        when(problemService.getProblemsByReferenceTypeId(eq(id))).thenReturn(createProblems());

        val response = mvc.getAndMapResponse(
            URL + "/by-entity-id?id=" + id, new TypeReference<List<ProblemResponse>>(){}
        );

        assertThat(response).isEqualTo(problems);
    }

    @Test
    public void getProblemsByUserTransactionId_returnsProblemsFromService() throws Exception {
        val id = UUID.randomUUID();
        val problems = createProblems();
        when(problemService.getProblemsByUserTransactionId(eq(id))).thenReturn(createProblems());

        val response = mvc.getAndMapResponse(
            URL + "/by-user-transaction-id?id=" + id.toString(), new TypeReference<List<ProblemResponse>>(){}
        );

        assertThat(response).isEqualTo(problems);
    }

    private List<ProblemResponse> createProblems() {
        return Arrays.asList(new ProblemResponse());
    }
}
