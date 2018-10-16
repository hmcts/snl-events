package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.search.HearingSearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.search.HearingSpecification;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.search.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("hearing")
public class HearingController {

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private HearingPartService hearingPartService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingInfo getHearingById(@PathVariable("id") UUID id) {
        return new HearingInfo(hearingRepository.findOne(id));
    }

    @PutMapping(path = "/{hearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingToSession(
        @PathVariable UUID hearingId,
        @RequestBody HearingSessionRelationship assignment) throws Exception {

        UserTransaction ut = hearingPartService.assignHearingToSessionWithTransaction(hearingId, assignment);

        return ok(ut);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Iterable<HearingInfo> fetchAllHearingParts(
        @RequestParam("isListed") Optional<Boolean> isListed,
        @RequestParam(value = "page", required = false) Optional<Integer> page,
        @RequestParam(value = "size", required = false) Optional<Integer> size,
        @RequestBody(required = false) HearingSearchCriteria hearingSearchCriteria) {
        PageRequest pageRequest =
            (page.isPresent() && size.isPresent()) ? new PageRequest(page.get(), size.get()) : null;

        if (isListed.isPresent()) {
            // return hearingPartService.getAllHearingPartsThat(isListed.get(), hearingSearchCriteria, pageRequest);
        }

        // return hearingPartService.getAllHearingParts();

        HearingSpecification hs = new HearingSpecification(new SearchCriteria("caseNumber", ":", "123"));

        return hearingRepository.findAll(Specifications.where(hs)).stream().map(HearingInfo::new).collect(Collectors.toList());
    }
}
