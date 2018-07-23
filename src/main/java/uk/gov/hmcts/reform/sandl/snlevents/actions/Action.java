package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.IAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.Transactionable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;

public abstract class Action implements IAction, RulesProcessable, Transactionable {

    @Autowired
    protected FactsMapper factsMapper;

    @Autowired
    protected ObjectMapper objectMapper;
}
