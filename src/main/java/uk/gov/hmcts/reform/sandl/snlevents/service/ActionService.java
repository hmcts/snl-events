package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.ActivityLoggable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ActivityLogRepository;

import java.util.UUID;

import javax.transaction.Transactional;

@Service
public class ActionService {

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Transactional
    public UserTransaction execute(Action action) {
        UUID transactionId = action.getUserTransactionId();

        action.getAndValidateEntities();

        // TO DO move action.generateUserTransactionData() here in order to save entity state before act

        if (userTransactionService.isAnyBeingTransacted(action.getAssociatedEntitiesIds())) {
            return userTransactionService.transactionConflicted(transactionId);
        }

        action.act();

        UserTransaction ut = userTransactionService.startTransaction(
            transactionId,
            action.generateUserTransactionData());

        if (action instanceof RulesProcessable) {
            ((RulesProcessable) action).generateFactMessages()
                .forEach(factMessage -> rulesService.postMessage(transactionId, factMessage));
        }

        if (action instanceof ActivityLoggable) {
            activityLogRepository.save(((ActivityLoggable) action).getActivities());
        }

        return userTransactionService.rulesProcessed(ut);
    }

}
