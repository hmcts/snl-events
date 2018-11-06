package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;

import java.util.List;

public interface RulesProcessable {
    List<FactMessage> generateFactMessages();
}
