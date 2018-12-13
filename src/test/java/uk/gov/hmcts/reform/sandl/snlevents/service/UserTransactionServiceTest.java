package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.config.ScheduledRollbackConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionDataRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus.COMPLETE;
import static uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus.NOT_STARTED;

@RunWith(SpringRunner.class)
public class UserTransactionServiceTest {
    private final UUID someId = new UUID(1, 1);
    UserTransaction ut;

    @InjectMocks
    private UserTransactionService userTransactionService;

    @Mock
    private UserTransactionRepository userTransactionRepository;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private UserTransactionDataRepository userTransactionDataRepository;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RevertChangesManager revertChangesManager;

    @Mock
    private Clock clock;

    @Mock
    private ScheduledRollbackConfiguration rollbackConfig;

    @Before
    public void setUp() {
        this.ut = new UserTransaction(someId,
            UserTransactionStatus.STARTED,
            IN_PROGRESS);
        when(userTransactionRepository.save(any(UserTransaction.class))).thenAnswer(returnsFirstArg());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(rollbackConfig.getTimeoutIntervalInMinutes()).thenReturn(1);
        when(rollbackConfig.isEnabled()).thenReturn(true);
    }

    @Test
    public void getUserTransactionById_should_call_findOne_on_userTransactionRepository() {
        when(userTransactionRepository.findOne(any(UUID.class))).thenReturn(ut);
        this.userTransactionService.getUserTransactionById(someId);
        assertThat(this.userTransactionService.getUserTransactionById(someId)).isEqualTo(ut);
    }

    @Test
    public void startTransaction_should_set_UserTransaction_Id() {
        UserTransaction userTransaction = this.userTransactionService.startTransaction(someId, new ArrayList<>());
        assertThat(userTransaction.getId()).isEqualTo(someId);
    }

    @Test
    public void startTransaction_should_create_UserTransaction_with_User_Transaction_Status_started() {
        UserTransaction userTransaction = this.userTransactionService.startTransaction(someId, new ArrayList<>());
        assertThat(userTransaction.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
    }

    @Test
    public void startTransaction_should_pass_User_Transaction_Data() {
        List<UserTransactionData> userTransactionData = generateUserTransactionsData(2);
        UserTransaction userTransaction = this.userTransactionService.startTransaction(someId, userTransactionData);
        assertThat(userTransaction.getUserTransactionDataList().size()).isEqualTo(userTransactionData.size());
    }

    @Test
    public void rulesProcessed_should_set_processingStatus_to_complete() {
        UserTransaction userTransaction = this.userTransactionService.rulesProcessed(ut);

        assertThat(userTransaction.getRulesProcessingStatus()).isEqualTo(COMPLETE);
        assertThat(userTransaction).isEqualTo(ut);
    }


    @Test
    public void commit_should_set_transactionStatus_to_committed() {
        when(userTransactionRepository.findOne(this.someId)).thenReturn(ut);

        boolean successfulResult = this.userTransactionService.commit(this.someId);

        assertThat(successfulResult).isEqualTo(true);
    }

    @Test
    public void rollback_should_set_transactionStatus_to_rolledback() {
        when(userTransactionRepository.findOne(this.someId)).thenReturn(ut);

        boolean successfulResult = this.userTransactionService.rollback(this.someId);

        assertThat(successfulResult).isEqualTo(true);
    }

    @Test
    public void transactionConflicted_should_set_transactionStatus_to_conflict() {
        UserTransaction userTransaction = this.userTransactionService.transactionConflicted(this.someId);
        assertThat(userTransaction.getStatus()).isEqualTo(UserTransactionStatus.CONFLICT);
    }

    @Test
    public void transactionConflicted_should_set_ProcessingStatus_to_not_started() {
        UserTransaction userTransaction = this.userTransactionService.transactionConflicted(this.someId);
        assertThat(userTransaction.getRulesProcessingStatus())
            .isEqualTo(NOT_STARTED);
    }

    @Test
    public void getTimedOutTransactions_should_return_entries() {
        Instant now = Instant.now();
        when(userTransactionRepository.getAllByStartedAtBeforeAndStatusNotInOrderByStartedAtAsc(any(),
            eq(new UserTransactionStatus[] {UserTransactionStatus.ROLLEDBACK, UserTransactionStatus.COMMITTED})))
            .thenReturn(Arrays.asList(
                new UserTransaction(UUID.randomUUID(), UserTransactionStatus.STARTED, NOT_STARTED),
                new UserTransaction(UUID.randomUUID(), UserTransactionStatus.CONFLICT, NOT_STARTED),
                new UserTransaction(UUID.randomUUID(), UserTransactionStatus.INPROGRESS, NOT_STARTED)
            ));
        when(clock.instant()).thenReturn(now);

        List<UserTransaction> userTransactions = this.userTransactionService.getTimedOutTransactions();

        assertThat(userTransactions.size()).isEqualTo(3);
    }

    private List<UserTransactionData> generateUserTransactionsData(int size) {
        List<UserTransactionData> userTransactions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UserTransactionData ut = new UserTransactionData(
                "some-entity",
                new UUID(i, i),
                "some-before-data",
                "some-action",
                "some-counter-action",
                i);

            userTransactions.add(ut);
        }

        return userTransactions;
    }
}
