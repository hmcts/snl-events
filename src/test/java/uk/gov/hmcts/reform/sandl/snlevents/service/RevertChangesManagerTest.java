package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    EntityManager entityManager;

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
        when(hearingPartRepository.findById(any(UUID.class))).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_updateHearingPart() throws IOException {
        when(hearingPartRepository.findById(any(UUID.class))).thenReturn(createHearingPart());
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartUpsert();
        revertChangesManager.revertChanges(transaction);
        //verify(rulesService, times(1)) @TODO to be considered while working on multiple sessions
        //    .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test(expected = EntityNotFoundException.class)
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

    private UUID createUuid() {
        return UUID.randomUUID();
    }

    private Session createSession() {
        return new Session();
    }
}
