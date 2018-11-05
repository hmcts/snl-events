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
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    public void revertChanges_handleHearing_whenCounterActionIsDelete() throws IOException {
        Hearing h = createHearing();
        when(hearingRepository.findById(any(UUID.class))).thenReturn(Optional.of(h));
        val transaction = createUserTransactionWithHearingDelete();
        revertChangesManager.revertChanges(transaction);
        verify(hearingRepository, times(1)).deleteById(h.getId());
    }

    @Test
    public void revertChanges_handleHearing_whenCounterActionIsUpdate() throws IOException {
        Hearing h = createHearing();
        Hearing prevHearing = createHearing();
        prevHearing.setCaseTitle("PRE");
        when(hearingRepository.findById(any())).thenReturn(Optional.of(h));
        val transaction = createUserTransactionWithHearingUpdate();
        when(objectMapper.readValue(anyString(), eq(Hearing.class))).thenReturn(prevHearing);

        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(isNull(), eq(RulesService.UPSERT_HEARING_PART), isNull());

        verify(hearingRepository, times(1)).save(prevHearing);
    }

    @Test
    public void revertChanges_deletesSessionInRuleService_ifCounterActionIsDelete() throws IOException {
        when(sessionRepository.findById(any())).thenReturn(Optional.of(createSession()));
        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(isNull(), eq(RulesService.DELETE_SESSION), isNull());
    }

    @Test
    public void revertChanges_deleteHearingPart() throws IOException {
        when(hearingPartRepository.findByIdWithHearing(any())).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);
        verify(rulesService, times(1))
            .postMessage(isNull(), eq(RulesService.DELETE_HEARING_PART), isNull());
    }

    @Test
    public void revertChanges_updateHearingPart() throws IOException {
        when(hearingPartRepository.findByIdWithHearing(any())).thenReturn(createHearingPart());
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(createHearingPart());
        val transaction = createUserTransactionWithHearingPartUpsert();
        revertChangesManager.revertChanges(transaction);
        //verify(rulesService, times(1)) @TODO to be considered while working on multiple sessions
        //    .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test(expected = EntityNotFoundException.class)
    public void revertChanges_throwsException_whenSessionIsNotFound() throws IOException {
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());
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
        data.setBeforeData("");
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
