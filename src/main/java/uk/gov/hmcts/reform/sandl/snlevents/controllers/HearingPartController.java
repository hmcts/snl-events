package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart.AmendScheduledListingAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart.AssignHearingPartToSessionAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.CreateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.DeleteListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.UpdateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendScheduledListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/hearing-part")
public class HearingPartController {
    @Autowired
    HearingPartService hearingPartService;

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HearingTypeRepository hearingTypeRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private HearingMapper hearingMapper;

    @Autowired
    private StatusServiceManager statusServiceManager;

    @Autowired
    private StatusConfigService statusConfigService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingPartResponse getHearingPartById(@PathVariable("id") UUID id) {
        return new HearingPartResponse(hearingPartRepository.findOne(id));
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<HearingPartResponse> fetchAllHearingParts(
        @RequestParam("isListed") Optional<Boolean> isListed
    ) {
        if (isListed.isPresent()) {
            return hearingPartService.getAllHearingPartsThat(isListed.get());
        }

        return hearingPartService.getAllHearingParts();
    }

    @PutMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createHearingPartAction(
        @Valid @RequestBody CreateHearingRequest createHearingRequest
    ) {
        Action action = new CreateListingRequestAction(
            createHearingRequest,
            hearingMapper,
            hearingTypeRepository,
            caseTypeRepository,
            hearingRepository,
            statusConfigService,
            statusServiceManager,
            entityManager
        );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateHearingPart(@Valid @RequestBody UpdateListingRequest updateListingRequest) {
        Action action = new UpdateListingRequestAction(
            updateListingRequest,
            entityManager,
            objectMapper,
            hearingRepository,
            hearingPartRepository,
            statusConfigService
            );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "/{hearingPartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingPartToSession(
        @PathVariable UUID hearingPartId,
        @RequestBody HearingPartSessionRelationship assignment) {

        Action action = new AssignHearingPartToSessionAction(hearingPartId, assignment,
            hearingPartRepository, sessionRepository, statusConfigService, statusServiceManager,
            entityManager, objectMapper
        );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "/amend-scheduled-listing", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity amendScheduledListing(
        @Valid @RequestBody AmendScheduledListingRequest amendScheduledListingRequest) {

        Action action = new AmendScheduledListingAction(
            amendScheduledListingRequest,
            hearingPartRepository,
            entityManager,
            objectMapper,
            hearingRepository
        );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PostMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteHearingPart(@Valid @RequestBody DeleteListingRequest request) {
        Action action = new DeleteListingRequestAction(
            request, hearingRepository, entityManager, objectMapper
        );

        return ok(actionService.execute(action));
    }

}
