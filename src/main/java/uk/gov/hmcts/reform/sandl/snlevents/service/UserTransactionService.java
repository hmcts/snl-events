package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.config.ScheduledRollbackConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionDataRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

@Service
public class UserTransactionService {

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @Autowired
    private UserTransactionDataRepository userTransactionDataRepository;

    @Autowired
    private RevertChangesManager revertChangesManager;

    @Autowired
    private ScheduledRollbackConfiguration scheduledRollbackConfiguration;

    @Autowired
    @Setter
    private Clock clock;

    public UserTransaction getUserTransactionById(UUID id) {
        return userTransactionRepository.findOne(id);
    }

    @Transactional
    public UserTransaction startTransaction(UUID transactionId, List<UserTransactionData> userTransactionDataList) {
        UserTransaction ut = new UserTransaction(transactionId,
            UserTransactionStatus.STARTED,
            UserTransactionRulesProcessingStatus.IN_PROGRESS);

        ut.addUserTransactionData(userTransactionDataList);

        return userTransactionRepository.save(ut);
    }

    @Transactional
    public UserTransaction rulesProcessed(UserTransaction ut) {
        ut.setRulesProcessingStatus(UserTransactionRulesProcessingStatus.COMPLETE);
        return userTransactionRepository.save(ut);
    }

    @Transactional
    public boolean commit(UUID id) {
        UserTransaction ut = userTransactionRepository.findOne(id);
        if (!canRollbackOrCommit(ut)) {
            return false;
        }
        ut.setStatus(UserTransactionStatus.COMMITTED);
        userTransactionRepository.save(ut);
        return true;
    }

    @Transactional
    public boolean rollback(UUID id) {
        UserTransaction ut = userTransactionRepository.findOne(id);
        if (!canRollbackOrCommit(ut)) {
            return false;
        }
        revertChangesManager.revertChanges(ut);
        //todo implement and check optimistic locking using version, don't need to detach entity
        ut.setStatus(UserTransactionStatus.ROLLEDBACK);
        userTransactionRepository.save(ut);
        return true;
    }

    public boolean isAnyBeingTransacted(UUID... entityIds) {
        return userTransactionDataRepository
            .existsByEntityIdInAndUserTransaction_StatusEquals(
                Arrays.stream(entityIds).filter(Objects::nonNull).collect(Collectors.toList()),
                UserTransactionStatus.STARTED);
    }

    public UserTransaction transactionConflicted(UUID transactionId) {
        return new UserTransaction(transactionId,
            UserTransactionStatus.CONFLICT,
            UserTransactionRulesProcessingStatus.NOT_STARTED);
    }

    public List<UserTransaction> getTimedOutTransactions() {
        final OffsetDateTime fiveMinutesAgo = OffsetDateTime.now(clock).minusMinutes(
            scheduledRollbackConfiguration.getTimeoutIntervalInMinutes()
        );
        return userTransactionRepository.getAllByStartedAtBeforeAndStatusNotInOrderByStartedAtAsc(
            fiveMinutesAgo,
            new UserTransactionStatus[] {UserTransactionStatus.ROLLEDBACK, UserTransactionStatus.COMMITTED}
        );
    }

    private boolean canRollbackOrCommit(UserTransaction userTransaction) {
        return !userTransaction.getStatus().equals(UserTransactionStatus.ROLLEDBACK)
            && !userTransaction.getStatus().equals(UserTransactionStatus.COMMITTED);
    }
}
