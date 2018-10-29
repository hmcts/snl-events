package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.UnlistHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("hearing")
public class HearingController {

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private HearingPartService hearingPartService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingInfo getHearingById(@PathVariable("id") UUID id) {
        return new HearingInfo(hearingRepository.findOne(id));
    }

    @GetMapping(path = "/{id}/with-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingWithSessionsResponse getHearingByIdWithSessions(@PathVariable("id") UUID id) {
        return new HearingWithSessionsResponse(hearingRepository.findOne(id));
    }

    @PutMapping(path = "/{hearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assignHearingToSession(
        @PathVariable UUID hearingId,
        @RequestBody HearingSessionRelationship assignment) throws Exception {

        UserTransaction ut = hearingPartService.assignHearingToSessionWithTransaction(hearingId, assignment);

        return ok(ut);
    }

    @PutMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unlist(@RequestBody UnlistHearingRequest unlistHearingRequest) {
        // Todo move to service?
        Action action = new UnlistHearingAction(
            unlistHearingRequest,
            hearingRepository,
            hearingPartRepository,
            objectMapper
        );

        UserTransaction ut = actionService.execute(action);

        return ok(ut);
    }
}
