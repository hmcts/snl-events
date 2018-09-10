package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;

public interface RulesProcessable {
    FactMessage generateFactMessage();
}
