package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.actions.session.AmendSessionAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchSessionSelectColumn;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ActionService actionService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SessionInfo getSessionById(@PathVariable("id") UUID id) {
        return sessionService.getSessionInfoById(id);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Session> fetchAllSessions() {
        return sessionService.getSessions();
    }

    @GetMapping(path = "", params = "date", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SessionInfo> fetchSessions(
        @RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        return sessionService.getSessionsFromDate(date);
    }

    @GetMapping(path = "", params = {"startDate", "endDate"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public SessionWithHearings fetchSessionsWithHearingsForDates(
        @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {
        return sessionService.getSessionsWithHearingsForDates(startDate, endDate);
    }

    @PostMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SessionSearchResponse> fetchSessionsWithHearingsForDates(
        @RequestParam(value = "page", required = false) Optional<Integer> page,
        @RequestParam(value = "size", required = false) Optional<Integer> size,
        @RequestParam(value = "sort", required = false) Optional<String> sort,
        @RequestBody(required = false) List<SearchCriteria> searchCriteriaList) {

        SearchSessionSelectColumn orderByColumn = null;
        Sort.Direction direction = null;
        if (sort.isPresent()) {
            val sortPair = sort.get().split(":");
            orderByColumn = SearchSessionSelectColumn.fromString(sortPair[0]);
            direction = Sort.Direction.fromString(sortPair[1]);
        }

        PageRequest pageRequest =
            (page.isPresent() && size.isPresent()) ? new PageRequest(page.get(), size.get()) : new PageRequest(0, 10);


        return sessionService.searchForSession(searchCriteriaList, pageRequest, orderByColumn, direction);
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity insertSession(@Valid @RequestBody UpsertSession upsertSession) throws IOException {

        String msg = factsMapper.mapCreateSessionToRuleJsonMessage(upsertSession);

        UserTransaction ut = sessionService.saveWithTransaction(upsertSession);

        rulesService.postMessage(ut.getId(), RulesService.INSERT_SESSION, msg);

        ut = userTransactionService.rulesProcessed(ut);

        return ok(ut);
    }

    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateSession(@RequestBody UpsertSession upsertSession) throws IOException {
        UserTransaction ut = sessionService.updateSession(upsertSession);
        return ok(ut);
    }

    @PostMapping(path = "/amend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity amendSession(@Valid @RequestBody AmendSessionRequest amendSessionRequest) {
        val action = new AmendSessionAction(
            amendSessionRequest,
            sessionRepository,
            entityManager,
            objectMapper
        );

        return ok(actionService.execute(action));
    }

    @GetMapping(path = "/judge-diary", produces = MediaType.APPLICATION_JSON_VALUE)
    public SessionWithHearings getJudgeDiary(
        @RequestParam("judge") String judge,
        @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {
        return sessionService.getSessionJudgeDiaryForDates(judge, startDate, endDate);
    }
}
