package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.AssignSessionsToHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AdjournHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VacateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.WithdrawHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.HearingForListingColumn;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("hearing")
public class HearingController {

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private HearingService hearingService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StatusServiceManager statusServiceManager;

    @Autowired
    private StatusConfigService statusConfigService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingInfo getHearingById(@PathVariable("id") UUID id) {
        return new HearingInfo(hearingRepository.findOne(id));
    }

    @GetMapping(path = "/for-listing", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<HearingForListingResponse> getCanBeListed(
        @RequestParam(value = "page", required = false) Optional<Integer> page,
        @RequestParam(value = "size", required = false) Optional<Integer> size,
        @RequestParam(value = "sortByProperty", required = false) Optional<String> sortByProperty,
        @RequestParam(value = "sortByDirection", required = false) Optional<String> sortByDirection) {

        HearingForListingColumn orderByColumn = null;
        Sort.Direction direction = null;
        if (sortByProperty.isPresent() && sortByDirection.isPresent()) {
            orderByColumn = HearingForListingColumn.fromString(sortByProperty.get());
            direction = Sort.Direction.fromString(sortByDirection.get());
        }

        return hearingService.getHearingsForListing(page, size, orderByColumn, direction);
    }

    @GetMapping(path = "/{id}/for-amendment", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingSearchResponseForAmendment getHearingByIdForAmendment(@PathVariable("id") UUID id) {
        return hearingService.get(id);
    }

    @GetMapping(path = "/{id}/with-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingWithSessionsResponse getHearingByIdWithSessions(@PathVariable("id") UUID id) {
        Hearing hearing = hearingRepository.findOne(id);
        HearingWithSessionsResponse response = new HearingWithSessionsResponse(hearing);
        response.setPossibleActions(statusServiceManager.getPossibleActions(response));

        return response;
    }

    @PutMapping(path = "/{hearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingToSession(
        @PathVariable UUID hearingId,
        @RequestBody HearingSessionRelationship assignment) {

        Action action = new AssignSessionsToHearingAction(
            hearingId, assignment, hearingRepository, hearingPartRepository, sessionRepository, statusConfigService,
            statusServiceManager, entityManager, objectMapper
        );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<HearingSearchResponse> searchHearings(
        @RequestParam(value = "page", required = false) Optional<Integer> page,
        @RequestParam(value = "size", required = false) Optional<Integer> size,
        @RequestBody(required = false) List<SearchCriteria> searchCriteriaList) {
        PageRequest pageRequest = (page.isPresent() && size.isPresent())
            ? new PageRequest(page.get(), size.get())
            : new PageRequest(0, 10);

        return hearingService.search(searchCriteriaList, pageRequest);
    }

    @PutMapping(path = "/unlist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unlist(@RequestBody UnlistHearingRequest unlistHearingRequest) {
        return ok(hearingService.unlist(unlistHearingRequest));
    }

    @PutMapping(path = "/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity withdraw(@RequestBody WithdrawHearingRequest withdrawHearingRequest) {
        return ok(hearingService.withdraw(withdrawHearingRequest));
    }

    @PutMapping(path = "/adjourn", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity adjourn(@Valid @RequestBody AdjournHearingRequest adjournHearingRequest) {
        return ok(hearingService.adjourn(adjournHearingRequest));
    }

    @PutMapping(path = "/vacate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity vacate(@RequestBody VacateHearingRequest vacateHearingRequest) {
        return ok(hearingService.vacate(vacateHearingRequest));
    }
}
