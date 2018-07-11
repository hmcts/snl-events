package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RevertChangesManagerTests {
    @TestConfiguration
    static class RevertChangesManagerTestContextConfiguration {
        @Bean
        public RevertChangesManager revertChangesManager() {
            return new RevertChangesManager();
        }
    }

    @Autowired
    RevertChangesManager revertChangesManager;

    @MockBean
    SessionRepository sessionRepository;

    @MockBean
    RulesService rulesService;

    @MockBean
    FactsMapper factsMapper;

    @Test
    public void revertChanges_deletesSessionInRuleService_ifCounterActionIsDelete() throws IOException {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_SESSION), anyString());
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

    private Session createSession() {
        return new Session();
    }
}
