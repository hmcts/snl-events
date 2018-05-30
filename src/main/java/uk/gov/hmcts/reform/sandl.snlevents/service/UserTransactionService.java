package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ProblemResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserTransactionService {

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    public UserTransaction getUserTransactionById(UUID id) {
        return userTransactionRepository.getOne(id);
    }

    public UserTransaction startTransaction(UUID transactionId) {
        UserTransaction ut = new UserTransaction(transactionId,
            UserTransactionStatus.STARTED,
            UserTransactionRulesProcessingStatus.IN_PROGRESS,
            null);

        return userTransactionRepository.save(ut);
    }

    public UserTransaction rulesProcessed(UserTransaction ut) {
        ut.setRulesProcessingStatus(UserTransactionRulesProcessingStatus.COMPLETE);
        return userTransactionRepository.save(ut);
    }
}
