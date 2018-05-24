package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.service.ProblemService;

import java.util.List;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ProblemResponse> getProblems() {
        return problemService.getProblems();
    }

    @GetMapping(path = "by-reference-type-id", params = "id", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ProblemResponse> getProblemsByReferenceTypeId(@RequestParam("id") String id) {
        return problemService.getProblemsByReferenceTypeId(id);
    }
}
