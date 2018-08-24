package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.List;
import java.util.UUID;

public interface UserTransactionable {
    List<UserTransactionData> generateUserTransactionData();
    UUID getUserTransactionId();
    UUID[] getAssociatedEntitiesIds();
}
