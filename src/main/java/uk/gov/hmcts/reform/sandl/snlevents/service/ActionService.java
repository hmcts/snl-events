package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;

import java.util.UUID;

import javax.transaction.Transactional;

@Service
public class ActionService {

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    RulesService rulesService;

    @Transactional
    public UserTransaction execute(Action action) {
        UUID transactionId = action.getUserTransactionId();

        action.getAndValidateEntities();

        if (userTransactionService.isAnyBeingTransacted(action.getAssociatedEntitiesIds())) {
            return userTransactionService.transactionConflicted(transactionId);
        }

        action.act();

        UserTransaction ut = userTransactionService.startTransaction(
            transactionId,
            action.generateUserTransactionData());

        if (action instanceof RulesProcessable) {
            rulesService.postMessage(transactionId,((RulesProcessable) action).generateFactMessage());
        }

        return userTransactionService.rulesProcessed(ut);
    }

}
