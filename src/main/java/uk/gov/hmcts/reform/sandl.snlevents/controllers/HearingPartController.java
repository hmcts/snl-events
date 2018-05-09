package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

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

import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;

@RestController
@RequestMapping("/hearing-part")
public class HearingPartController {

    @Autowired
    HearingPartService hearingPartService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<HearingPart> fetchAllHearingParts() {
        return hearingPartService.getAllHearingParts();
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity upsertHearingPart(@RequestBody CreateHearingPart createHearingPart) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createHearingPart.getId());
        hearingPart.setCaseNumber(createHearingPart.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPart.getCaseTitle());
        hearingPart.setCaseType(createHearingPart.getCaseType());
        hearingPart.setHearingType(createHearingPart.getHearingType());
        hearingPart.setDuration(createHearingPart.getDuration());
        hearingPart.setScheduleStart(createHearingPart.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPart.getScheduleEnd());

        return ok(hearingPartService.save(hearingPart));
    }

    @PutMapping(path = "/{hearingPartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingPartToSession(@PathVariable UUID hearingPartId,
                                                       @RequestBody HearingPartSessionRelationship assignment) {
        HearingPart hearingPart = hearingPartService.assignHearingPartToSession(hearingPartId,
            assignment.getSessionId());

        return ok(hearingPart);
    }
}
