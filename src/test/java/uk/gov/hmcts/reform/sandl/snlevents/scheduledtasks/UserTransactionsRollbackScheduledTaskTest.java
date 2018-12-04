package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class UserTransactionsRollbackScheduledTaskTest {

    @InjectMocks
    private UserTransactionsRollbackScheduledTask scheduledTasks;

    @Mock
    private UserTransactionService utService;

    @Test
    public void rollbackForgottenTransactions_forNoMatchingEntries_shouldNotRollbackAnything() {
        when(utService.getTimedOutTransactions()).thenReturn(Collections.emptyList());

        scheduledTasks.rollbackForgottenTransactions();

        verify(utService, times(0)).rollback(any());
    }

    @Test
    public void rollbackForgottenTransactions_forMatchingEntries_shouldCallRollback() {
        when(utService.getTimedOutTransactions()).thenReturn(Arrays.asList(
            new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.STARTED, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.INPROGRESS, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.CONFLICT, UserTransactionRulesProcessingStatus.IN_PROGRESS
            )
        ));

        scheduledTasks.rollbackForgottenTransactions();

        verify(utService, times(3)).rollback(any());
    }
}
