package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.CreateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.UpdateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private SessionRepository sessionRepository;

    @Autowired
    private ActionService actionService;

    @Autowired
    private FactsMapper factsMapper;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<HearingPart> fetchAllHearingParts(@RequestParam("isListed") Optional<Boolean> isListed) {
        if (isListed.isPresent()) {
            return hearingPartService.getAllHearingPartsThat(isListed.get());
        }

        return hearingPartService.getAllHearingParts();
    }

    @PutMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createHearingPartAction(@RequestBody CreateHearingPart createHearingPart) throws Exception {
        Action action = new CreateListingRequestAction(createHearingPart, hearingPartRepository);

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity upsertHearingPart(@RequestBody CreateHearingPart createHearingPart) throws IOException {

        String msg = factsMapper.mapCreateHearingPartToRuleJsonMessage(createHearingPart);
        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);

        HearingPart hearingPart = hearingPartService.findOne(createHearingPart.getId());
        if (hearingPart == null) {
            hearingPart = new HearingPart();
        }
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

        if (createHearingPart.getCreatedAt() == null) {
            hearingPart.setCreatedAt(OffsetDateTime.now());
        } else {
            hearingPart.setCreatedAt(createHearingPart.getCreatedAt());
        }
        hearingPart.setPriority(createHearingPart.getPriority());

        return ok(hearingPartService.save(hearingPart));
    }

    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateHearingPart(@RequestBody CreateHearingPart createHearingPart) {
        UserTransaction ut = actionService.execute(new UpdateListingRequestAction(createHearingPart, hearingPartRepository));
        return ok(ut);
    }

    @PutMapping(path = "/{hearingPartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingPartToSession(
        @PathVariable UUID hearingPartId,
        @RequestBody HearingPartSessionRelationship assignment) throws Exception {

        UserTransaction ut = hearingPartService.assignHearingPartToSessionWithTransaction(hearingPartId, assignment);

         // ut = actionService.execute(new AssignHearingPartToSessionAction(hearingPartId, assignment,
         //    hearingPartRepository, sessionRepository));
        return ok(ut);
    }
}
