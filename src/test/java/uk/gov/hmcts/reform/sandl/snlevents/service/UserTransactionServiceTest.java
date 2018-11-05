package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionDataRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUp() {
        this.ut = new UserTransaction(someId,
            UserTransactionStatus.STARTED,
            UserTransactionRulesProcessingStatus.IN_PROGRESS);
        when(userTransactionRepository.save(any(UserTransaction.class))).thenAnswer(returnsFirstArg());
    }

    @Test
    public void getUserTransactionById_should_call_findById_on_userTransactionRepository() {
        when(userTransactionRepository.findById(any(UUID.class))).thenReturn(Optional.of(ut));
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

        assertThat(userTransaction.getRulesProcessingStatus()).isEqualTo(UserTransactionRulesProcessingStatus.COMPLETE);
        assertThat(userTransaction).isEqualTo(ut);
    }


    @Test
    public void commit_should_set_transactionStatus_to_committed() {
        when(userTransactionRepository.findById(this.someId)).thenReturn(Optional.of(ut));

        UserTransaction userTransaction = this.userTransactionService.commit(this.someId);

        assertThat(userTransaction.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);
    }

    @Test
    public void rollback_should_set_transactionStatus_to_rolledback() {
        when(userTransactionRepository.findById(this.someId)).thenReturn(Optional.of(ut));

        UserTransaction userTransaction = this.userTransactionService.rollback(this.someId);

        assertThat(userTransaction.getStatus()).isEqualTo(UserTransactionStatus.ROLLEDBACK);
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
            .isEqualTo(UserTransactionRulesProcessingStatus.NOT_STARTED);
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
