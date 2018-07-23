package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.List;

public interface IAction {
    void initialize();
    void act();
    void validate() throws Exception;
}
