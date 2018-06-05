package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
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
    private FactsMapper factsMapper;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<HearingPart> fetchAllHearingParts() {
        return hearingPartService.getAllHearingParts();
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity upsertHearingPart(@RequestBody CreateHearingPart createHearingPart) throws IOException {

        String msg = factsMapper.mapCreateHearingPartToRuleJsonMessage(createHearingPart);
        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createHearingPart.getId());
        hearingPart.setCaseNumber(createHearingPart.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPart.getCaseTitle());
        hearingPart.setCaseType(createHearingPart.getCaseType());
        hearingPart.setHearingType(createHearingPart.getHearingType());
        hearingPart.setDuration(createHearingPart.getDuration());
        hearingPart.setScheduleStart(createHearingPart.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPart.getScheduleEnd());
        if (createHearingPart.getCreatedAt() == null) {
            hearingPart.setCreatedAt(OffsetDateTime.now());
        } else {
            hearingPart.setCreatedAt(createHearingPart.getCreatedAt());
        }

        return ok(hearingPartService.save(hearingPart));
    }

    @PutMapping(path = "/{hearingPartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingPartToSession(
        @PathVariable UUID hearingPartId,
        @RequestBody HearingPartSessionRelationship assignment) throws IOException {

        HearingPart hearingPart = hearingPartService.assignHearingPartToSession(hearingPartId, assignment);

        return ok(hearingPart);
    }
}
