package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

public interface IAction {
    void initialize();
    void act();
    void validate() throws Exception;
}
