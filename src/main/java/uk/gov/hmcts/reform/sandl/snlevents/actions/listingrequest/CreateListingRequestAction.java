package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateListingRequestAction extends Action implements RulesProcessable {

    protected CreateHearingPartRequest createHearingPartRequest;
    protected HearingPart hearingPart;

    protected HearingPartRepository hearingPartRepository;
    protected HearingTypeRepository hearingTypeRepository;
    protected CaseTypeRepository caseTypeRepository;

    public CreateListingRequestAction(CreateHearingPartRequest createHearingPartRequest,
                                      HearingPartRepository hearingPartRepository,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository) {
        this.createHearingPartRequest = createHearingPartRequest;
        this.hearingPartRepository = hearingPartRepository;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
    }

    @Override
    public void act() {
        hearingPart = new HearingPart();

        hearingPart.setId(createHearingPartRequest.getId());
        hearingPart.setCaseNumber(createHearingPartRequest.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPartRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(createHearingPartRequest.getCaseTypeCode());
        hearingPart.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(createHearingPartRequest.getHearingTypeCode());
        hearingPart.setHearingType(hearingType);
        hearingPart.setDuration(createHearingPartRequest.getDuration());
        hearingPart.setScheduleStart(createHearingPartRequest.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPartRequest.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(createHearingPartRequest.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(createHearingPartRequest.getReservedJudgeId());
        hearingPart.setPriority(createHearingPartRequest.getPriority());

        hearingPart = hearingPartRepository.save(hearingPart);
    }

    @Override
    public void getAndValidateEntities() { }

    @Override
    public FactMessage generateFactMessage() {
        String msg;
        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
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
        return this.createHearingPartRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {this.createHearingPartRequest.getId()};
    }
}
