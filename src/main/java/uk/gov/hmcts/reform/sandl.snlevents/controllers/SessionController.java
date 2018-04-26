package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.sandl.snlevents.model.Session;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @RequestMapping(path = "/sessions", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<Session> fetchAllSessions() {
        List sessions = sessionService.getSessions();

        return sessions;
    }

    @RequestMapping(path = "/sessions", params = "date",  method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<Session> fetchSessions(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List sessions = sessionService.getSessionsFromDate(date);

        return sessions;
    }
}
