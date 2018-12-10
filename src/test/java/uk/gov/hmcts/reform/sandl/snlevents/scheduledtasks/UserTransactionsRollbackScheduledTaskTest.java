package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void verifyThat_rollbackForgottenTransactions_logsInformationOfNotCompletedRollback() {
        // setup logger
        Logger logger = (Logger) LoggerFactory.getLogger(UserTransactionsRollbackScheduledTask.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        final UserTransaction userTransaction = new UserTransaction(
            UUID.randomUUID(), UserTransactionStatus.STARTED, UserTransactionRulesProcessingStatus.IN_PROGRESS
        );
        when(utService.getTimedOutTransactions())
            .thenReturn(Collections.singletonList(userTransaction));
        when(utService.rollback(any()))
            .thenReturn(false);

        scheduledTasks.rollbackForgottenTransactions();

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.size()).isEqualTo(3);
        assertThat(logsList.get(2).getFormattedMessage()).contains(userTransaction.getId().toString());
    }
}
