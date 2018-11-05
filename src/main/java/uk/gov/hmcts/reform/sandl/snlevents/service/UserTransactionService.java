package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionDataRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

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

    public UserTransaction getUserTransactionById(UUID id) {
        return userTransactionRepository.findById(id).orElse(null);
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
    public UserTransaction commit(UUID id) {
        UserTransaction ut = userTransactionRepository.findById(id).orElse(null);
        ut.setStatus(UserTransactionStatus.COMMITTED);
        return userTransactionRepository.save(ut);
    }

    @Transactional
    public UserTransaction rollback(UUID id) {
        UserTransaction ut = userTransactionRepository.findById(id).orElse(null);

        revertChangesManager.revertChanges(ut);
        //todo implement and check optimistic locking using version, don't need to detach entity
        ut.setStatus(UserTransactionStatus.ROLLEDBACK);

        return userTransactionRepository.save(ut);
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
}
