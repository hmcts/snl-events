package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.List;
import java.util.UUID;

public class CreateListingRequestAction extends Action implements RulesProcessable {
    @Override
    public void act() {

    }

    @Override
    public void getAndValidateEntities() {

    }

    @Override
    public FactMessage generateFactMessage() {
        return null;
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        return null;
    }

    @Override
    public UUID getUserTransactionId() {
        return null;
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[0];
    }
}
