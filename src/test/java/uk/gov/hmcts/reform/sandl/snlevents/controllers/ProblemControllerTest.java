package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.service.ProblemService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ProblemController.class)
public class ProblemControllerTest {
    public static final String URL = "/problems";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProblemService problemService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void getProblems_returnsProblemsFromService() throws Exception {
        val problems = createProblems();
        when(problemService.getProblems()).thenReturn(createProblems());

        val response = getMappedResponse(URL);

        assertThat(response).isEqualTo(problems);
    }

    @Test
    public void getProblemsByReferenceEntityId_returnsProblemsFromService() throws Exception {
        val id = UUID.randomUUID().toString();
        val problems = createProblems();
        when(problemService.getProblemsByReferenceTypeId(eq(id))).thenReturn(createProblems());

        val response = getMappedResponse(URL + "/by-entity-id?id=" + id);

        assertThat(response).isEqualTo(problems);
    }

    @Test
    public void getProblemsByUserTransactionId_returnsProblemsFromService() throws Exception {
        val id = UUID.randomUUID();
        val problems = createProblems();
        when(problemService.getProblemsByUserTransactionId(eq(id))).thenReturn(createProblems());

        val response = getMappedResponse(URL + "/by-user-transaction-id?id=" + id.toString());

        assertThat(response).isEqualTo(problems);
    }

    private Object getMappedResponse(String url) throws Exception {
        val response = mvc
            .perform(get(url))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, new TypeReference<List<ProblemResponse>>(){});
    }

    private List<ProblemResponse> createProblems() {
        return new ArrayList<>(Arrays.asList(new ProblemResponse()));
    }
}
