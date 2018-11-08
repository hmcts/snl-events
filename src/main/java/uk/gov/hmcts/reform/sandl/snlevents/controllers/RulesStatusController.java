package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReloadRulesService;

import java.io.IOException;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping("rules")
public class RulesStatusController {

    @Autowired
    private ReloadRulesService reloadRulesService;

    @GetMapping(path = "/status")
    public ResponseEntity getStatuses() {
        return ok(reloadRulesService.getReloadStatuses());
    }
}
