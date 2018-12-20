package uk.gov.hmcts.reform.sandl.snlevents.model.request;

public enum UserTransactionAction {
    COMMIT("commit"),
    ROLLBACK("rollback");

    private final String action;
    UserTransactionAction(String action) {
        this.action = action;
    }
}
