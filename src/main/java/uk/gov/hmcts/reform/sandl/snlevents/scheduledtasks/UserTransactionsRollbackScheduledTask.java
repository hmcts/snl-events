package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.util.List;
import javax.transaction.Transactional;

@Component
@Transactional
@EnableScheduling
@ConditionalOnProperty(
    prefix = "scheduler.auto-rollback",
    name = "enabled", havingValue = "true")
public class UserTransactionsRollbackScheduledTask {
    private static final Logger logger = LoggerFactory.getLogger(UserTransactionsRollbackScheduledTask.class);

    @Autowired
    private UserTransactionService userTransactionService;

    @Scheduled(fixedRate = 60 * 1000) // check every minute
    public void rollbackForgottenTransactions() {
        logger.info("Initiating rollback of forgotten transactions ");
        List<UserTransaction> timedOutTransactions = userTransactionService.getTimedOutTransactions();
        logger.info("Found: {}", timedOutTransactions.size());
        timedOutTransactions.forEach(ut -> {
            boolean succeeded = userTransactionService.rollback(ut.getId());
            if (!succeeded) {
                logger.info("Automatic rollback failed for user-transaction[ {} ] status: {}",
                    ut.getId(), ut.getStatus().name()
                );
            }
        });
    }
}
