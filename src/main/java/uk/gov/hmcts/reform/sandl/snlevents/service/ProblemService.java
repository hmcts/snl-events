package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblem;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemReferenceResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;
import uk.gov.hmcts.reform.sandl.snlevents.transformers.FactTransformer;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    @Autowired
    private ProblemRepository problemRepository;

    private final Function<ProblemReference, ProblemReferenceResponse> problemReferenceDbToResponse =
        (ProblemReference pr) -> {
            ProblemReferenceResponse response = new ProblemReferenceResponse();

            response.setEntity(pr.getEntity());
            response.setEntityId(pr.getEntityId());
            response.setDescription(pr.getDescription());

            return response;
        };

    public final Function<Problem, ProblemResponse> problemDbToResponse = (Problem p) -> {
        ProblemResponse response = new ProblemResponse();

        response.setId(p.getId());
        response.setType(p.getType());
        response.setMessage(p.getMessage());
        response.setSeverity(p.getSeverity());
        response.setCreatedAt(p.getCreatedAt());
        response.setReferences(
            p.getReferences()
                .stream()
                .map(problemReferenceDbToResponse)
                .collect(Collectors.<ProblemReferenceResponse>toList())
        );

        return response;
    };

    private final Function<CreateProblemReference, ProblemReference> problemReferenceCreateToDb =
        (CreateProblemReference cpr) -> {
            ProblemReference transformed = new ProblemReference();
            transformed.setEntity(FactTransformer.transformToEntityName(cpr.getFact()));
            transformed.setEntityId(cpr.getFactId());
            transformed.setDescription(cpr.getDescription());

            return transformed;
        };

    public Problem problemCreateToDb(CreateProblem cp) {
        Problem transformed = new Problem();

        transformed.setId(cp.getId());
        transformed.setType(cp.getType());
        transformed.setSeverity(cp.getSeverity());
        transformed.setMessage(cp.getMessage());
        transformed.setCreatedAt(cp.getCreatedAt());
        transformed.setReferences(
            cp.getReferences()
                .stream()
                .map(problemReferenceCreateToDb)
                .collect(Collectors.<ProblemReference>toList())
        );

        return transformed;
    }

    public Iterable<ProblemResponse> getProblems(Pageable pegable) {
        if (pegable == null) {
            return problemRepository
                .getAllSortedBySeverityAndCreatedAt()
                .stream()
                .map(problemDbToResponse)
                .collect(Collectors.toList());
        }

        return problemRepository
            .getAllSortedBySeverityAndCreatedAt(pegable)
            .map(problem -> problemDbToResponse.apply(problem));
    }

    public Problem save(Problem problem) {
        return problemRepository.save(problem);
    }

    public void removeIfExist(String id) {
        if (problemRepository.exists(id)) {
            problemRepository.delete(id);
        }
    }

    public List<ProblemResponse> getProblemsByReferenceTypeId(String referenceEntityId) {
        List<Problem> problems = problemRepository.getProblemsByReferenceEntityId(referenceEntityId);
        return problems.stream()
            .map(problemDbToResponse)
            .collect(Collectors.toList());
    }


    public List<ProblemResponse> getProblemsByUserTransactionId(UUID userTransactionId) {
        List<Problem> problems = problemRepository.getProblemsByUserTransactionId(userTransactionId);
        return problems.stream()
            .map(problemDbToResponse)
            .collect(Collectors.toList());
    }
}
