package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
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

    private StatusesMock statusesMock = new StatusesMock();

    @Test(expected = EntityNotFoundException.class)
    public void revertChanges_throwsException_whenSessionIsNotFound() {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(null);
        revertChangesManager.revertChanges(createUserTransactionWithSessionDelete());
    }

    @Test
    public void revertChanges_updatesSessionInRuleService_whenCounterActionIsUpdate() throws IOException {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        when(factsMapper.mapDbSessionToRuleJsonMessage(any(Session.class), any()).getSessionFact()).thenReturn("");
        when(objectMapper.readValue("{}", Session.class)).thenReturn(createSession());

        val transaction = createUserTransactionWithSessionUpdate();
        revertChangesManager.revertChanges(transaction);

        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.UPSERT_SESSION), anyString());
    }

    @Test
    public void revertChanges_deletesSessionInRuleService_whenCounterActionIsDelete() {
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());

        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);

        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_SESSION), anyString());
    }

    @Test
    public void revertChanges_createsHearingPartInRuleService_whenCounterActionIsCreate() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(factsMapper.mapHearingToRuleJsonMessage(any(HearingPart.class))).thenReturn("");
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(createHearingPart());

        val transaction = createUserTransactionWithHearingPartCreate();
        revertChangesManager.revertChanges(transaction);

        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_updatesHearingPartInRuleService_whenCounterActionIsUpdate() throws IOException {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(factsMapper.mapHearingToRuleJsonMessage(any(HearingPart.class))).thenReturn("");
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(createHearingPart());

        val transaction = createUserTransactionWithHearingPartUpdate();
        revertChangesManager.revertChanges(transaction);

        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.UPSERT_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_deletesHearingPartInRuleService_whenCounterActionIsDelete() {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());

        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);

        verify(rulesService, times(1))
            .postMessage(any(UUID.class), eq(RulesService.DELETE_HEARING_PART), anyString());
    }

    @Test
    public void revertChanges_updatesSession_whenCounterActionIsUpdate() throws IOException {
        Session sessionBeingRolledBack = createSession();
        sessionBeingRolledBack.setVersion(1L);
        sessionBeingRolledBack.setDuration(Duration.ofHours(3));
        Session previousSession = createSession();
        previousSession.setId(sessionBeingRolledBack.getId());
        previousSession.setVersion(0L);
        previousSession.setDuration(Duration.ofHours(2));

        when(sessionRepository.findOne(any(UUID.class))).thenReturn(sessionBeingRolledBack);
        when(objectMapper.readValue("{}", Session.class)).thenReturn(previousSession);

        val transaction = createUserTransactionWithSessionUpdate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        Mockito.verify(entityManager).merge(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(sessionBeingRolledBack.getId());
        assertThat(captor.getValue().getVersion()).isEqualTo(1L);
        assertThat(captor.getValue().getDuration()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    public void revertChanges_deletesSession_whenCounterActionIsDelete() {
        Session sessionBeingRolledBack = createSession();
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(sessionBeingRolledBack);

        val transaction = createUserTransactionWithSessionDelete();
        revertChangesManager.revertChanges(transaction);

        verify(sessionRepository, times(1)).delete(sessionBeingRolledBack);
    }

    @Test
    public void revertChanges_createsHearing_whenCounterActionIsCreate() {
        Hearing hearingBeingRolledBack = createHearing();
        hearingBeingRolledBack.setDeleted(true);

        when(hearingRepository.getHearingByIdIgnoringWhereDeletedClause(any(UUID.class)))
            .thenReturn(hearingBeingRolledBack);

        val transaction = createUserTransactionWithHearingCreate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);
        Mockito.verify(entityManager).merge(captor.capture());
        assertThat(captor.getValue().isDeleted()).isFalse();
    }

    @Test
    public void revertChanges_updatesHearing_whenCounterActionIsUpdate() throws IOException {
        Hearing hearingBeingRolledBack = createHearing();
        hearingBeingRolledBack.setVersion(1L);
        hearingBeingRolledBack.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        Hearing previousHearing = createHearing();
        previousHearing.setId(hearingBeingRolledBack.getId());
        previousHearing.setVersion(0L);
        previousHearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));

        when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearingBeingRolledBack);
        when(objectMapper.readValue("{}", Hearing.class)).thenReturn(previousHearing);

        val transaction = createUserTransactionWithHearingUpdate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);
        Mockito.verify(entityManager).merge(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(hearingBeingRolledBack.getId());
        assertThat(captor.getValue().getVersion()).isEqualTo(1L);
        assertThat(captor.getValue().getStatus()).isEqualTo(
            statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
    }

    @Test
    public void revertChanges_deletesHearing_whenCounterActionIsDelete() {
        Hearing hearingBeingRolledBack = createHearing();
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearingBeingRolledBack);

        val transaction = createUserTransactionWithHearingDelete();
        revertChangesManager.revertChanges(transaction);

        verify(hearingRepository, times(1)).delete(hearingBeingRolledBack);
    }

    @Test
    public void revertChanges_createsHearingPart_whenCounterActionIsCreate() {
        HearingPart hearingPartBeingRolledBack = createHearingPart();
        hearingPartBeingRolledBack.setDeleted(true);

        when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(hearingPartBeingRolledBack);

        val transaction = createUserTransactionWithHearingPartCreate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);
        Mockito.verify(entityManager).merge(captor.capture());
        assertThat(captor.getValue().isDeleted()).isFalse();
    }

    @Test
    public void revertChanges_updatesHearingPart_whenCounterActionIsUpdate() throws IOException {
        HearingPart hearingPartBeingRolledBack = createHearingPart();
        hearingPartBeingRolledBack.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Adjourned));
        HearingPart previousHearingPart = createHearingPart();
        previousHearingPart.setId(hearingPartBeingRolledBack.getId());
        previousHearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));

        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(hearingPartBeingRolledBack);
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(previousHearingPart);

        val transaction = createUserTransactionWithHearingPartUpdate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);
        Mockito.verify(entityManager).merge(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(hearingPartBeingRolledBack.getId());
        assertThat(captor.getValue().getStatus()).isEqualTo(
            statusesMock.statusConfigService.getStatusConfig(Status.Listed));
    }

    @Test
    public void revertChanges_deletesHearingPart_whenCounterActionIsDelete() {
        HearingPart hearingPartBeingRolledBack = createHearingPart();
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(hearingPartBeingRolledBack);

        val transaction = createUserTransactionWithHearingPartDelete();
        revertChangesManager.revertChanges(transaction);

        verify(hearingPartRepository, times(1)).delete(hearingPartBeingRolledBack);
    }

    private UserTransaction createUserTransactionWithHearingCreate() {
        val data = new UserTransactionData();
        data.setEntity("hearing");
        data.setCounterAction("create");
        data.setBeforeData("{}");
        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingUpdate() {
        val data = new UserTransactionData();
        data.setEntity("hearing");
        data.setCounterAction("update");
        data.setBeforeData("{}");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingDelete() {
        val data = new UserTransactionData();
        data.setEntity("hearing");
        data.setCounterAction("delete");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingPartCreate() {
        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("create");
        data.setBeforeData("{}");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithHearingPartUpdate() {
        val data = new UserTransactionData();
        data.setEntity("hearingPart");
        data.setCounterAction("update");
        data.setBeforeData("{}");

        val transaction = new UserTransaction();
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

    private UserTransaction createUserTransactionWithSessionUpdate() {
        val data = new UserTransactionData();
        data.setEntity("session");
        data.setCounterAction("update");
        data.setBeforeData("{}");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private UserTransaction createUserTransactionWithSessionDelete() {
        val data = new UserTransactionData();
        data.setEntity("session");
        data.setCounterAction("delete");

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(data);

        return transaction;
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setVersion(0L);
        val h = new Hearing();
        h.setVersion(0L);

        hp.setHearingId(createUuid());
        hp.setHearing(h);

        return hp;
    }

    private Hearing createHearing() {
        val hp = new HearingPart();
        hp.setVersion(0L);
        val h = new Hearing();
        h.setVersion(0L);

        hp.setHearingId(createUuid());
        hp.setHearing(h);

        h.addHearingPart(hp);

        return h;
    }

    private Session createSession() {
        Session session = new Session();
        session.setVersion(0L);

        return session;
    }

    private UUID createUuid() {
        return UUID.randomUUID();
    }
}
