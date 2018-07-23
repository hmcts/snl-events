package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import javax.transaction.Transactional;
import java.util.List;

public class ActionService {

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    RulesService rulesService;

    @Transactional
    public UserTransaction execute(Action action) throws Exception {
        action.initialize();
        action.validate();

        if(userTransactionService.isAnyBeingTransacted(action.getAssociatedEntitiesIds())) {
            return userTransactionService.transactionConflicted(action.getUserTransactionId());
        };

        action.act();

        List<UserTransactionData> utdl = action.generateUserTransactionData();
        UserTransaction ut = userTransactionService.startTransaction(action.getUserTransactionId(),
            utdl);

        rulesService.postMessage(ut.getId(), action.generateFactMessage());

        return userTransactionService.rulesProcessed(ut);
    }

}
