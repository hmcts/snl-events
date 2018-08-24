package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class UpdateListingRequestAction extends Action implements RulesProcessable {

    private CreateHearingPart createHearingPart;
    private HearingPartRepository hearingPartRepository;


    @Override
    public void act() {
    }

    @Override
    public void getAndValidateEntities() {

    }

    @Override
    public FactMessage generateFactMessage() {
        return null;
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        return null;
    }

    @Override
    public UUID getUserTransactionId() {
        return null;
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {this.createHearingPart.getId()};
    }
}
