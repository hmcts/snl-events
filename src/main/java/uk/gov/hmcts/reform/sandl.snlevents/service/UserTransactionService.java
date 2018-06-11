package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionDataRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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
    public UserTransaction commit(UUID id) {
        UserTransaction ut = userTransactionRepository.findOne(id);
        ut.setStatus(UserTransactionStatus.COMMITTED);
        return userTransactionRepository.save(ut);
    }

    @Transactional
    public UserTransaction rollback(UUID id) throws IOException {
        UserTransaction ut = userTransactionRepository.findOne(id);

        revertChangesManager.revertChanges(ut);
        ut.setStatus(UserTransactionStatus.ROLLEDBACK);

        return userTransactionRepository.save(ut);
    }

    public boolean isBeingTransacted(UUID entityId) {
        if(entityId == null) {
            return false;
        } else {
            List<UserTransactionData> utd = userTransactionDataRepository.
                    findByEntityIdEqualsAndUserTransaction_StatusEquals(entityId, UserTransactionStatus.STARTED);
            return utd.size() > 0;
        }
    }
}
