package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblem;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemReferenceResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    @Autowired
    private ProblemRepository problemRepository;

    private Function<ProblemReference, ProblemReferenceResponse> problemReferenceDbToResponse =
        (ProblemReference pr) -> {
            ProblemReferenceResponse response = new ProblemReferenceResponse();
            response.setId(pr.getId());
            response.setType(pr.getType());
            response.setDescription(pr.getDescription());
            response.setProblemId(pr.getProblem().getId());
            return response;
        };

    public Function<Problem, ProblemResponse> problemDbToResponse = (Problem p) -> {
        ProblemResponse response = new ProblemResponse();
        response.setId(p.getId());
        response.setType(p.getType());
        response.setSeverity(p.getSeverity());
        response.setReferences(
            p.getReferences()
                .stream()
                .map(problemReferenceDbToResponse)
                .collect(Collectors.<ProblemReferenceResponse>toList())
        );
        return response;
    };

    private Function<CreateProblemReference, ProblemReference> problemReferenceCreateToDb =
        (CreateProblemReference cpr) -> {
            ProblemReference transformed = new ProblemReference();
            transformed.setId(cpr.getId().toString());
            transformed.setType(cpr.getType());
            transformed.setDescription(cpr.getDescription());
            return transformed;
        };

    public Function<CreateProblem, Problem> problemCreateToDb = (CreateProblem cp) -> {
        Problem transformed = new Problem();
        transformed.setId(cp.getId().toString());
        transformed.setType(cp.getType());
        transformed.setSeverity(cp.getSeverity());
        transformed.setReferences(
            cp.getReferences()
                .stream()
                .map(problemReferenceCreateToDb)
                .collect(Collectors.<ProblemReference>toList())
        );
        transformed.getReferences().forEach(pr -> pr.setProblem(transformed));
        return transformed;
    };

    public List<ProblemResponse> getProblems() {
        final List<Problem> problems = problemRepository.findAll();

        return problems.stream()
            .map(problemDbToResponse)
            .collect(Collectors.toList());
    }

    public Problem save(Problem problem) {
        return problemRepository.save(problem);
    }

    public void removeIfExist(String id) {
        if (problemRepository.exists(id)) {
            problemRepository.delete(id);
        }
    }
}
