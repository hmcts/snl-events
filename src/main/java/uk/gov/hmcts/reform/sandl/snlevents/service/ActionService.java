package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ActionService {

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    RulesService rulesService;

    @Transactional
    public UserTransaction execute(Action action) throws Exception {
        UUID transactionId = action.getUserTransactionId();

        action.initialize();
        action.validate();

        if(userTransactionService.isAnyBeingTransacted(action.getAssociatedEntitiesIds())) {
            return userTransactionService.transactionConflicted(transactionId);
        }

        action.act();

        UserTransaction ut = userTransactionService.startTransaction(
            transactionId,
            action.generateUserTransactionData());

        if(action instanceof RulesProcessable) {
            rulesService.postMessage(transactionId, action.generateFactMessage());
        }

        return userTransactionService.rulesProcessed(ut);
    }

}
