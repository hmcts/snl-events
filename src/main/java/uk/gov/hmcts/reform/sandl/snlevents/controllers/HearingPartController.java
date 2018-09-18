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
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.CreateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.DeleteListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.UpdateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;
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
    private RulesService rulesService;

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FactsMapper factsMapper;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingPart getHearingPartById(@PathVariable("id") UUID id) {
        return hearingPartRepository.findOne(id);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<HearingPart> fetchAllHearingParts(@RequestParam("isListed") Optional<Boolean> isListed) {
        if (isListed.isPresent()) {
            return hearingPartService.getAllHearingPartsThat(isListed.get());
        }

        return hearingPartService.getAllHearingParts();
    }

    @PutMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createHearingPartAction(@Valid @RequestBody CreateHearingPart createHearingPart) throws Exception {
        Action action = new CreateListingRequestAction(createHearingPart, hearingPartRepository);

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity upsertHearingPart(@RequestBody CreateHearingPart createHearingPart) throws IOException {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createHearingPart.getId());
        hearingPart.setCaseNumber(createHearingPart.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPart.getCaseTitle());
        hearingPart.setCaseType(createHearingPart.getCaseType());
        hearingPart.setHearingType(createHearingPart.getHearingType());
        hearingPart.setDuration(createHearingPart.getDuration());
        hearingPart.setScheduleStart(createHearingPart.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPart.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(createHearingPart.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(createHearingPart.getReservedJudgeId());
        hearingPart.setPriority(createHearingPart.getPriority());

        hearingPart = hearingPartService.save(hearingPart);

        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);

        return ok(hearingPart);
    }

    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateHearingPart(@RequestBody UpdateListingRequest updateListingRequest) {
        Action action = new UpdateListingRequestAction(updateListingRequest,
            hearingPartRepository, entityManager, objectMapper);

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "/{hearingPartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingPartToSession(
        @PathVariable UUID hearingPartId,
        @RequestBody HearingPartSessionRelationship assignment) throws Exception {

        UserTransaction ut = hearingPartService.assignHearingPartToSessionWithTransaction(hearingPartId, assignment);

        return ok(ut);
    }

    @PostMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteHearingPart(
        @RequestBody DeleteListingRequest request
    ) {
        Action action = new DeleteListingRequestAction(
            request, hearingPartRepository, entityManager, objectMapper
        );

        return ok(actionService.execute(action));
    }

}
