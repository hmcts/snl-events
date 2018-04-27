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
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
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
    private RoomRepository roomRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SessionMapper sessionMapper;

    @RequestMapping(path = "/sessions", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<Session> fetchAllSessions() {
        return sessionService.getSessions();
    }

    @RequestMapping(path = "/sessions", params = "date",  method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody List<SessionInfo> fetchSessions(
        @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return sessionService.getSessionsFromDate(date);
    }

    @RequestMapping(path = "/sessions", method = RequestMethod.PUT, consumes = {"application/json"})
    public ResponseEntity insertSession(@RequestBody CreateSession createSession) throws IOException {

        String msg = sessionMapper.mapSessionToRuleJsonMessage(createSession);
        rulesService.postMessage(RulesService.INSERT_SESSION, msg);

        Session session = new Session();
        session.setId(createSession.getId());
        session.setDuration(createSession.getDuration());
        session.setStart(createSession.getStart());
        //session.setCaseType(createSession.getCaseType());

        Room room = roomRepository.findOne(createSession.getRoomId());
        if (room != null) {
            session.setRoom(room);
        }

        Person person = personRepository.findOne(createSession.getPersonId());
        if (person != null) {
            session.setPerson(person);
        }

        sessionService.save(session);

        return ok("OK");
    }
}
