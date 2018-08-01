package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblem;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemReferenceResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ProblemServiceTest {
    public static final String ID = "id";
    public static final String MESSAGE = "msg";
    public static final String SEVERITY = "sev";
    public static final String DESCRIPTION = "desc";
    public static final String ENTITY = "ent";
    public static final String ENTITY_ID = "eid";
    public static final String PROBLEM_ID = "problem-id";
    private static final String TYPE = "type";

    @InjectMocks
    private ProblemService problemService;

    @Mock
    private ProblemRepository problemRepository;

    @Test
    public void getProblemsReturnsEmptyListWhenRepositoryReturnsEmptyList() {
        when(problemRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProblemResponse> ret = problemService.getProblems();
        assertThat(ret).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getProblemsReturnsProblemsFromRepository() {
        when(problemRepository.findAll()).thenReturn(createProblems());

        List<ProblemResponse> expectedProblemResponses = createProblemResponses();

        List<ProblemResponse> problemResponses = problemService.getProblems();
        assertThat(problemResponses).isEqualTo(expectedProblemResponses);
    }

    @Test
    public void saveCallsSaveOnRepository() {
        Problem problem = createProblem();
        problemService.save(problem);

        verify(problemRepository, times(1)).save(problem);
    }

    @Test
    public void removeIfExistDeletesProblemsIfItExists() {
        when(problemRepository.exists(PROBLEM_ID)).thenReturn(true);

        problemService.removeIfExist(PROBLEM_ID);

        verify(problemRepository, times(1)).delete(PROBLEM_ID);
    }

    @Test
    public void removeIfExistDoesNothingWhenProblemDoesNotExist() {
        when(problemRepository.exists(PROBLEM_ID)).thenReturn(false);

        problemService.removeIfExist(PROBLEM_ID);

        verify(problemRepository, times(0)).delete(PROBLEM_ID);
    }

    @Test
    public void getProblemsByReferenceTypeIdReturnsProblemResponsesFromRepository() {
        when(problemRepository.getProblemsByReferenceEntityId(ENTITY_ID)).thenReturn(createProblems());

        List<ProblemResponse> problems = problemService.getProblemsByReferenceTypeId(ENTITY_ID);
        assertThat(problems).isEqualTo(createProblemResponses());
    }

    @Test
    public void getProblemsByUserTransactionIdReturnsProblemResponsesFromRepository() {
        UUID uuid = UUID.randomUUID();
        when(problemRepository.getProblemsByUserTransactionId(uuid)).thenReturn(createProblems());

        List<ProblemResponse> problems = problemService.getProblemsByUserTransactionId(uuid);
        assertThat(problems).isEqualTo(createProblemResponses());
    }

    @Test
    public void problemCreateToDb_tranformsCreateProblemToProblem() {
        val expectedProblem = createProblem();

        val problem = problemService.problemCreateToDb(createCreateProblem());

        assertThat(problem).isEqualTo(expectedProblem);
    }

    private CreateProblem createCreateProblem() {
        val cp = new CreateProblem();
        cp.setReferences(Arrays.asList(createCreateProblemReference()));
        cp.setId(ID);
        cp.setMessage(MESSAGE);
        cp.setSeverity(SEVERITY);
        cp.setType(TYPE);

        return cp;
    }

    private CreateProblemReference createCreateProblemReference() {
        val cpr = new CreateProblemReference();
        cpr.setFact(ENTITY);
        cpr.setFactId(ENTITY_ID);
        cpr.setDescription(DESCRIPTION);

        return cpr;
    }

    private Problem createProblem() {
        Problem problem = new Problem();
        problem.setId(ID);
        problem.setMessage(MESSAGE);
        problem.setSeverity(SEVERITY);
        problem.setType(TYPE);

        ProblemReference problemReference = new ProblemReference();
        problemReference.setDescription(DESCRIPTION);
        problemReference.setEntity(ENTITY);
        problemReference.setEntityId(ENTITY_ID);

        problem.setReferences(Arrays.asList(problemReference));

        return problem;
    }

    private List<Problem> createProblems() {
        return Arrays.asList(createProblem());
    }

    private ProblemResponse createProblemResponse() {
        ProblemResponse problemResponse = new ProblemResponse();
        problemResponse.setId(ID);
        problemResponse.setMessage(MESSAGE);
        problemResponse.setSeverity(SEVERITY);
        problemResponse.setType(TYPE);

        ProblemReferenceResponse problemReference = new ProblemReferenceResponse();
        problemReference.setDescription(DESCRIPTION);
        problemReference.setEntity(ENTITY);
        problemReference.setEntityId(ENTITY_ID);

        problemResponse.setReferences(Arrays.asList(problemReference));

        return problemResponse;
    }

    private List<ProblemResponse> createProblemResponses() {
        return Arrays.asList(createProblemResponse());
    }
}
