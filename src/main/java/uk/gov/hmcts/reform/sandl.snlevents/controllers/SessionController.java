package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.SessionMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Session;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Controller
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private SessionMapper sessionMapper;

    @RequestMapping(path = "/sessions", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<Session> fetchAllSessions() {
        return sessionService.getSessions();
    }

    @RequestMapping(path = "/sessions", params = "date",  method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<Session> fetchSessions(
        @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return sessionService.getSessionsFromDate(date);
    }

    @RequestMapping(path = "/sessions", method = RequestMethod.PUT, consumes = {"application/json"})
    public ResponseEntity insertSession(@RequestBody Session session) throws IOException {

        String msg = sessionMapper.mapSessionToRuleJsonMessage(session);
        rulesService.postMessage(RulesService.INSERT_SESSION, msg);

        sessionService.save(session);

        return ok("");
    }
}
