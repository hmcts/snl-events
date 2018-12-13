package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ActivityLogRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
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

    @Mock
    ActivityLogRepository activityLogRepository;

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
        verifyActivityLogRepositoryWasCalled(transaction.getId());
    }

    @Test
    public void revertChanges_createsHearingPartInRuleService_whenCounterActionIsCreate() throws IOException {
        when(hearingPartRepository.getHearingPartByIdIgnoringWhereDeletedClause(any(UUID.class)))
            .thenReturn(createHearingPart());
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
        verifyActivityLogRepositoryWasCalled(transaction.getId());
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
        verifyActivityLogRepositoryWasCalled(transaction.getId());
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
        Mockito.verify(hearingRepository).save(captor.capture());
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
        verifyActivityLogRepositoryWasCalled(transaction.getId());
    }

    @Test
    public void revertChanges_createsHearingPart_whenCounterActionIsCreate() {
        HearingPart hearingPartBeingRolledBack = createHearingPart();
        hearingPartBeingRolledBack.setDeleted(true);

        when(hearingPartRepository.getHearingPartByIdIgnoringWhereDeletedClause(any(UUID.class)))
            .thenReturn(hearingPartBeingRolledBack);

        val transaction = createUserTransactionWithHearingPartCreate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
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

    @Test
    public void revertChanges_updatesListingRequest_whenCounterActionIsUpdate_toListed() throws IOException {
        Hearing hearingBeingRolledBack = createHearing();
        hearingBeingRolledBack.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));

        Hearing previousHearing = createHearing();
        previousHearing.setId(hearingBeingRolledBack.getId());
        previousHearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));

        HearingPart hearingPartBeingRolledBack1 = createHearingPart();
        hearingPartBeingRolledBack1.setHearing(hearingBeingRolledBack);
        hearingPartBeingRolledBack1.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));

        Session session = createSession();

        HearingPart previousHearingPart1 = createHearingPart();
        previousHearingPart1.setId(hearingPartBeingRolledBack1.getId());
        previousHearingPart1.setHearing(hearingPartBeingRolledBack1.getHearing());
        previousHearingPart1.setSession(session);
        previousHearingPart1.setStart(OffsetDateTime.now());
        previousHearingPart1.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));

        HearingPart hearingPartBeingRolledBack2 = createHearingPart();
        hearingPartBeingRolledBack2.setHearing(hearingBeingRolledBack);
        hearingPartBeingRolledBack1.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));

        HearingPart previousHearingPart2 = createHearingPart();
        previousHearingPart2.setId(hearingPartBeingRolledBack2.getId());
        previousHearingPart2.setHearing(hearingPartBeingRolledBack2.getHearing());
        previousHearingPart2.setSession(session);
        previousHearingPart2.setStart(OffsetDateTime.now());
        previousHearingPart2.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));

        when(sessionRepository.findOne(any(UUID.class))).thenReturn(session);
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(hearingBeingRolledBack);
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(hearingPartBeingRolledBack1,
            hearingPartBeingRolledBack2);
        when(objectMapper.readValue("{}", Hearing.class)).thenReturn(previousHearing);
        when(objectMapper.readValue("{}", HearingPart.class)).thenReturn(previousHearingPart1,
            previousHearingPart2);

        val transaction = createUserTransactionWithMultiSessionHearingUpdate();
        revertChangesManager.revertChanges(transaction);

        ArgumentCaptor<Hearing> hearingCaptor = ArgumentCaptor.forClass(Hearing.class);
        ArgumentCaptor<HearingPart> hearingPartCaptor = ArgumentCaptor.forClass(HearingPart.class);
        InOrder verifyInOrder = inOrder(entityManager, entityManager);

        verifyInOrder.verify(entityManager, times(1)).merge(hearingCaptor.capture());
        assertThat(hearingCaptor.getValue().getStatus()).isEqualTo(
            statusesMock.statusConfigService.getStatusConfig(Status.Listed));

        verifyInOrder.verify(entityManager, times(2)).merge(hearingPartCaptor.capture());
        hearingPartCaptor.getAllValues().forEach(hp -> {
            assertThat(hp.getStatus()).isEqualTo(
                statusesMock.statusConfigService.getStatusConfig(Status.Listed));
            assertThat(hp.getSessionId()).isNotNull();
            assertThat(hp.getStart()).isNotNull();
        });
    }

    private void verifyActivityLogRepositoryWasCalled(UUID transactionId) {
        verify(activityLogRepository, times(1)).deleteActivityLogByUserTransactionId(transactionId);
    }

    private UserTransaction createUserTransactionWithMultiSessionHearingUpdate() {
        val session = new UserTransactionData();
        session.setEntity("session");
        session.setCounterAction("unlock");
        session.setCounterActionOrder(0);

        val hearing = new UserTransactionData();
        hearing.setEntity("hearing");
        hearing.setCounterAction("update");
        hearing.setBeforeData("{}");
        hearing.setCounterActionOrder(1);

        val hearingPart1 = new UserTransactionData();
        hearingPart1.setEntity("hearingPart");
        hearingPart1.setCounterAction("update");
        hearingPart1.setBeforeData("{}");
        hearingPart1.setCounterActionOrder(2);

        val hearingPart2 = new UserTransactionData();
        hearingPart2.setEntity("hearingPart");
        hearingPart2.setCounterAction("update");
        hearingPart2.setBeforeData("{}");
        hearingPart2.setCounterActionOrder(2);

        val transaction = new UserTransaction();
        transaction.addUserTransactionData(hearing);
        transaction.addUserTransactionData(hearingPart1);
        transaction.addUserTransactionData(hearingPart2);

        return transaction;
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
        hp.setId(UUID.randomUUID());
        hp.setVersion(0L);
        val h = new Hearing();
        h.setId(UUID.randomUUID());
        h.setVersion(0L);

        hp.setHearingId(createUuid());
        hp.setHearing(h);

        return hp;
    }

    private Hearing createHearing() {
        val h = new Hearing();
        h.setId(UUID.randomUUID());
        h.setVersion(0L);

        return h;
    }

    private Session createSession() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setVersion(0L);

        return session;
    }

    private UUID createUuid() {
        return UUID.randomUUID();
    }
}
