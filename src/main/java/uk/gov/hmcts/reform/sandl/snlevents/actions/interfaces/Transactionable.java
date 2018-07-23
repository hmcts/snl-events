package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.List;
import java.util.UUID;

public interface Transactionable {
    List<UserTransactionData> generateUserTransactionData() throws Exception;
    UUID getUserTransactionId();
    UUID[] getAssociatedEntitiesIds();
}
