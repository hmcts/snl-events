package uk.gov.hmcts.reform.sandl.snlevents.model.db;

public enum UserTransactionStatus {
    STARTED,
    PESSIMISTICLY_LOCKED,
    COMMITTED,
    ROLLEDBACK,
}
