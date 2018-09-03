package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateListingRequestAction extends Action implements RulesProcessable {

    protected CreateHearingPart createHearingPart;
    protected HearingPart hearingPart;

    protected HearingPartRepository hearingPartRepository;

    public CreateListingRequestAction(CreateHearingPart createHearingPart,
                                      HearingPartRepository hearingPartRepository) {
        this.createHearingPart = createHearingPart;
        this.hearingPartRepository = hearingPartRepository;
    }

    @Override
    public void act() {
        hearingPart = new HearingPart();

        hearingPart.setId(createHearingPart.getId());
        hearingPart.setCaseNumber(createHearingPart.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPart.getCaseTitle());
        hearingPart.setCaseType(createHearingPart.getCaseType());
        hearingPart.setHearingType(createHearingPart.getHearingType());
        hearingPart.setDuration(createHearingPart.getDuration());
        hearingPart.setScheduleStart(createHearingPart.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPart.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(createHearingPart.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(createHearingPart.getReservedJudgeId());
        hearingPart.setPriority(createHearingPart.getPriority());

        hearingPartRepository.save(hearingPart);
    }

    @Override
    public void getAndValidateEntities() {

    }

    @Override
    public FactMessage generateFactMessage() {
        String msg = null;
        try {
            msg = factsMapper.mapCreateHearingPartToRuleJsonMessage(createHearingPart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.add(new UserTransactionData("hearingPart",
            hearingPart.getId(),
            null,
            "create",
            "delete",
            0)
        );

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return this.createHearingPart.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {this.createHearingPart.getId()};
    }
}
