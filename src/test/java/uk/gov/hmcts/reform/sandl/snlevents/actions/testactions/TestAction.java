package uk.gov.hmcts.reform.sandl.snlevents.actions.testactions;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.List;
import java.util.UUID;

public class TestAction extends Action {
    @Override
    public void act() {

    }

    @Override
    public void getAndValidateEntities() {

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
