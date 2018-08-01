package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;

@RestController
public class TimeController {

    @Autowired
    RulesService rulesService;

    @Autowired
    FactsMapper factsMapper;

    @PutMapping("/time")
    public ResponseEntity upsert(@RequestBody DateTimePartValue dateTimePartValue) throws IOException {

        rulesService.postMessage(String.format("upsert-%s", dateTimePartValue.getTimeType()),
            factsMapper.mapTimeToRuleJsonMessage(dateTimePartValue));

        return ResponseEntity.ok("OK");
    }
}
