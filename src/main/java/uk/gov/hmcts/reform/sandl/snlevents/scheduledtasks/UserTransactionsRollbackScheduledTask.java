package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.util.List;
import javax.transaction.Transactional;

@Slf4j
@Component
@Transactional
@EnableScheduling
@ConditionalOnProperty(
    prefix = "scheduler.auto-rollback",
    name = "enabled", havingValue = "true")
public class UserTransactionsRollbackScheduledTask {

    @Autowired
    private UserTransactionService userTransactionService;

    @Scheduled(fixedRate = 60 * 1000) // check every minute
    public void rollbackPendingTransactions() {
        log.info("Initiating rollback of pending transactions ");
        List<UserTransaction> timedOutTransactions = userTransactionService.getTimedOutTransactions();
        log.info("Found: {}", timedOutTransactions.size());
        timedOutTransactions.forEach(ut -> {
            boolean succeeded = userTransactionService.rollback(ut.getId());
            if (!succeeded) {
                log.info("Automatic rollback failed for user-transaction[ {} ] status: {}",
                    ut.getId(), ut.getStatus().name()
                );
            }
        });
    }
}
