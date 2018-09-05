package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReferenceDataService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping("/reference")
public class ReferenceDataController {

    @Autowired
    private ReferenceDataService referenceDataService;

    @GetMapping(path = "/case-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getCaseTypes() {
        return ok(referenceDataService.getCaseTypes());
    }

    @GetMapping(path = "/session-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getSessionTypes() {
        return ok(referenceDataService.getSessionTypes());
    }

    @GetMapping(path = "/hearing-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getHearingTypes() {
        return ok(referenceDataService.getHearingTypes());
    }

    @GetMapping(path = "/room-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getRoomTypes() {
        return ok(referenceDataService.getRoomTypes());
    }
}
