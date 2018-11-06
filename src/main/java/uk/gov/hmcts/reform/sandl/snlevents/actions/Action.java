package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.IAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.UserTransactionable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;

public abstract class Action implements IAction, UserTransactionable {

    protected FactsMapper factsMapper = new FactsMapper();

    protected ObjectMapper objectMapper;
}
