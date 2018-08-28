package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.UUID;
import javax.xml.ws.WebServiceException;

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
    RulesService rulesService;

    @Mock
    FactsMapper factsMapper;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void revertChanges_deletesSessionInRuleService_ifCounterActionIsDelete() throws IOException {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_SESSION), anyString());
    }

    @Test
    public void revertChanges_deleteHearingPart() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(new HearingPart());
        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_updateHearingPart() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(new HearingPart());
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(new HearingPart());
        val transaction = createUserTransactionWithHearingPartUpsert();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test(expected = WebServiceException.class)
    public void revertChanges_throwsException_whenSessionIsNotFound() throws IOException {
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
        val transaction = new UserTransaction();

        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("delete");
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingPartUpsert() {
        val transaction = new UserTransaction();

        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("update");
        data.setBeforeData("{}");
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private Session createSession() {
        return new Session();
    }
}
