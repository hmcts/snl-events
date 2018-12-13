package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.config.ScheduledRollbackConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Transactional
public class UserTransactionsTests extends BaseIntegrationTest {

    @Autowired
    UserTransactionRepository utRepository;

    @Autowired
    UserTransactionService utService;

    @Autowired
    EntityManager entityManager;

    @Mock
    Clock clock;

    @Mock
    private ScheduledRollbackConfiguration rollbackConfig;

    @Before
    public void setup() {
        // Setup
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        final Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        utService.setClock(this.clock);

        when(rollbackConfig.getTimeoutIntervalInMinutes()).thenReturn(5);
        when(rollbackConfig.isEnabled()).thenReturn(true);

        List<UserTransaction> transactions = new ArrayList<>(Arrays.asList(
            new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.STARTED, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.INPROGRESS, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.CONFLICT, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.COMMITTED, UserTransactionRulesProcessingStatus.IN_PROGRESS
            ), new UserTransaction(
                UUID.randomUUID(), UserTransactionStatus.ROLLEDBACK, UserTransactionRulesProcessingStatus.IN_PROGRESS
            )
        ));
        OffsetDateTime fiveMinAgo = OffsetDateTime.now(clock).minusMinutes(5).minusSeconds(1);
        transactions.forEach(ut -> {
            ut.setStartedAt(fiveMinAgo);
        });

        utRepository.save(transactions);
    }

    @Test
    public void getTimedOutTransactions_should_return_not_Committed_and_not_Rolledback_entires() {
        final List<UserTransaction> transactions = utService.getTimedOutTransactions();

        assertThat(transactions.size()).isEqualTo(3); // out of 5
    }

    @Test
    public void getTimedOutTransactions_should_return_not_Committed_and_not_Rolledback_entires_old_enough() {
        UserTransaction lastMinuteTransaction = new UserTransaction(
            UUID.randomUUID(), UserTransactionStatus.STARTED, UserTransactionRulesProcessingStatus.IN_PROGRESS);
        lastMinuteTransaction.setStartedAt(OffsetDateTime.now(clock).minusMinutes(1));
        utRepository.save(lastMinuteTransaction);

        final List<UserTransaction> transactions = utService.getTimedOutTransactions();

        assertThat(transactions.size()).isEqualTo(3); // out of 6
    }
}


