package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SessionController.class)
@Import(TestConfiguration.class)
public class SessionControllerTest {

    private static final String SESSION_URL = "/sessions";

    @MockBean
    private SessionService sessionService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RulesService rulesService;

    @MockBean
    private UserTransactionService userTransactionService;

    @MockBean
    private FactsMapper factsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void fetchAllSessions_returnsSessionsFromService() throws Exception {
        val sessions = createSessionList();
        when(sessionService.getSessions()).thenReturn(sessions);

        val response = mvc.getAndMapResponse(SESSION_URL, new TypeReference<List<Session>>(){});
        assertEquals(sessions, response);
    }

    @Test
    public void getSessionById_returnsSessionFromService() throws Exception {
        final Session session = createSession();
        final String uuidParam = "/9e8bb0a0-0b0f-463d-ae8e-67a09e12f0cc";

        when(sessionService.getSessionById(any(UUID.class))).thenReturn(session);

        val response = mvc.getAndMapResponse(SESSION_URL + uuidParam, Session.class);
        assertEquals(session, response);
    }

    @Test
    public void fetchSessions_returnsSessionsFromDate() throws Exception {
        val sessionsInfo = createSessionInfoList();
        when(sessionService.getSessionsFromDate(any(LocalDate.class))).thenReturn(sessionsInfo);

        val response = mvc.getAndMapResponse(
            SESSION_URL + "?date=15-05-2018", new TypeReference<List<SessionInfo>>(){}
        );
        assertEquals(sessionsInfo, response);
    }

    @Test
    public void fetchSessions_withInvalidDateFormat_shouldGiveBadRequest() throws Exception {
        mvc.getMockMvc().perform(get(SESSION_URL + "?date=2018-05-05"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void fetchSessionsWithHearingsForDates_returnsSessionsWithHearings() throws Exception {
        SessionWithHearings sessionWithHearings = new SessionWithHearings();

        when(sessionService.getSessionsWithHearingsForDates(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(sessionWithHearings);

        val response = mvc.getAndMapResponse(
            SESSION_URL + "?startDate=15-05-2018&endDate=15-05-2018",
            SessionWithHearings.class
        );
        assertEquals(sessionWithHearings, response);
    }

    @Test
    public void updateSession_returnsUserTransaction() throws Exception {
        UpsertSession upsertSession = new UpsertSession();
        UserTransaction userTransaction = new UserTransaction();

        when(sessionService.updateSession(upsertSession)).thenReturn(userTransaction);

        val response = mvc.putAndMapResponse(
            SESSION_URL + "/update", objectMapper.writeValueAsString(upsertSession), UserTransaction.class
        );
        assertEquals(userTransaction, response);
    }

    @Test
    public void insertSession_returnsUserTransaction() throws Exception {
        UpsertSession upsertSession = new UpsertSession();
        UserTransaction userTransaction = new UserTransaction();

        when(factsMapper.mapCreateSessionToRuleJsonMessage(upsertSession)).thenReturn("rules message");
        when(sessionService.saveWithTransaction(upsertSession)).thenReturn(userTransaction);
        when(userTransactionService.rulesProcessed(userTransaction)).thenReturn(userTransaction);

        val response = mvc.putAndMapResponse(
            SESSION_URL, objectMapper.writeValueAsString(upsertSession), UserTransaction.class
        );
        assertEquals(userTransaction, response);
    }

    @Test
    public void getJudgeDiary_returnSessionWithHearings() throws Exception {
        SessionWithHearings sessionWithHearings = new SessionWithHearings();
        when(sessionService.getSessionJudgeDiaryForDates(any(String.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(sessionWithHearings);

        val response = mvc.getAndMapResponse(
            SESSION_URL + "/judge-diary?judge=John&startDate=12-08-2018&endDate=12-08-2018",
            SessionWithHearings.class
        );
        assertEquals(sessionWithHearings, response);
    }

    private Session createSession() {
        return new Session();
    }

    private List<Session> createSessionList() {
        return Arrays.asList(createSession());
    }

    private List<SessionInfo> createSessionInfoList() {
        return Arrays.asList(new SessionInfo());
    }
}
