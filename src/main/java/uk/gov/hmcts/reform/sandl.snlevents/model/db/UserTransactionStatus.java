package uk.gov.hmcts.reform.sandl.snlevents.model.db;

public enum UserTransactionStatus {
    STARTED,
    PESSIMISTIC_LOCK,
    COMMITTED,
    ROLLBACK,
}
