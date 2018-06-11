package uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction;

public enum UserTransactionStatus {
    STARTED,
    INPROGRESS, // PESSIMISTICLY_LOCKED,
    COMMITTED,
    ROLLEDBACK,
    CANCELLED
}
