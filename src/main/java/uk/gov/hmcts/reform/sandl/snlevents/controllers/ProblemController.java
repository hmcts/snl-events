package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.service.ProblemService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProblemResponse> getProblems() {
        return problemService.getProblems();
    }

    @GetMapping(path = "by-entity-id", params = "id", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProblemResponse> getProblemsByReferenceEntityId(@RequestParam("id") String id) {
        return problemService.getProblemsByReferenceTypeId(id);
    }

    @GetMapping(path = "by-user-transaction-id", params = "id", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProblemResponse> getProblemsByUserTransactionId(@RequestParam("id") UUID id) {
        return problemService.getProblemsByUserTransactionId(id);
    }
}
