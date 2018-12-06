package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RevertChangesManagerTest {
    @InjectMocks
    RevertChangesManager revertChangesManager;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    HearingPartRepository hearingPartRepository;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    RulesService rulesService;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    EntityManager entityManager;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    FactsMapper factsMapper;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void revertChanges_handleHearing_whenCounterActionIsDelete() {
        Hearing h = createHearing();
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(h);
        val transaction = createUserTransactionWithHearingDelete();
        revertChangesManager.revertChanges(transaction);
        verify(hearingRepository, times(1)).delete(h.getId());
    }

    @Test
    public void revertChanges_handleHearing_whenCounterActionIsUpdate() throws IOException {
        Hearing h = createHearing();
        Hearing prevHearing = createHearing();
        prevHearing.setCaseTitle("PRE");
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(h);
        val transaction = createUserTransactionWithHearingUpdate();
        when(objectMapper.readValue(any(String.class), eq(Hearing.class))).thenReturn(prevHearing);

        revertChangesManager.revertChanges(transaction);
        verify(hearingRepository, times(1)).save(prevHearing);
    }

    @Test
    public void revertChanges_deletesSessionInRuleService_ifCounterActionIsDelete() {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        when(factsMapper.mapDbSessionToRuleJsonMessage(any(Session.class), any()).getSessionFact()).thenReturn("");
        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_SESSION), anyString());
    }

    @Test
    public void revertChanges_deleteHearingPart() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_updateHearingPart() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartUpsert();
        revertChangesManager.revertChanges(transaction);
        //verify(rulesService, times(1)) @TODO to be considered while working on multiple sessions
        //    .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test(expected = EntityNotFoundException.class)
    public void revertChanges_throwsException_whenSessionIsNotFound() {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(null);
        revertChangesManager.revertChanges(createUserTransactionWithSessionDelete());
    }

    private UserTransaction createUserTransactionWithSessionDelete() {
        val transaction = new UserTransaction();

        val data = new UserTransactionData();
        data.setEntity("session");
        data.setCounterAction("delete");
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingPartDelete() {
        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("delete");
        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingDelete() {
        val data = new UserTransactionData();
        data.setEntity("hearing");
        data.setCounterAction("delete");
        data.setBeforeData("{}");
        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingUpdate() {
        val transaction = new UserTransaction();

        val data = new UserTransactionData();
        data.setEntity("hearing");
        data.setCounterAction("update");
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingPartUpsert() {

        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("update");
        data.setBeforeData("{}");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setVersion(0L);
        val h = new Hearing();

        hp.setHearingId(createUuid());
        hp.setHearing(h);

        return hp;
    }

    private Hearing createHearing() {
        val hp = new HearingPart();
        hp.setVersion(0L);
        val h = new Hearing();

        hp.setHearingId(createUuid());
        hp.setHearing(h);

        h.addHearingPart(hp);

        return h;
    }

    private UUID createUuid() {
        return UUID.randomUUID();
    }

    private Session createSession() {
        return new Session();
    }
}
