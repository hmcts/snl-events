package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping("/reference")
public class ReferenceDataController {

    @GetMapping(path = "/caseType/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getCaseTypes() {
        List<SimpleDictionaryData> temporaryValues = createDictionaryData("caseType", 10);
        return ok(temporaryValues);
    }

    @GetMapping(path = "/sessionType/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getSessionTypes() {
        List<SimpleDictionaryData> temporaryValues = createDictionaryData("sessionType", 10);
        return ok(temporaryValues);
    }

    @GetMapping(path = "/hearingType/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getHearingTypes() {
        List<SimpleDictionaryData> temporaryValues = createDictionaryData("hearingType", 10);
        return ok(temporaryValues);
    }


    @GetMapping(path = "/roomType/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> getRoomTypes() {
        List<SimpleDictionaryData> temporaryValues = createDictionaryData("roomType", 5);
        return ok(temporaryValues);
    }

    private List<SimpleDictionaryData> createDictionaryData(String description, int maximumElements) {
        List<SimpleDictionaryData> temporaryValues = new ArrayList<>();
        for (int i = 0; i < maximumElements; i++) {
            temporaryValues.add(new SimpleDictionaryData("code" + i, i + "_" + description.toUpperCase()));
        }
        return temporaryValues;
    }
}
