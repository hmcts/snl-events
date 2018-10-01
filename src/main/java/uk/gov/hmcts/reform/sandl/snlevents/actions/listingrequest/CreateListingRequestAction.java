package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingPartMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
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
    protected HearingPartMapper hearingPartMapper;

    public CreateListingRequestAction(CreateHearingPartRequest createHearingPartRequest,
                                      HearingPartMapper hearingPartMapper,
                                      HearingPartRepository hearingPartRepository,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository) {
        this.createHearingPartRequest = createHearingPartRequest;
        this.hearingPartMapper = hearingPartMapper;
        this.hearingPartRepository = hearingPartRepository;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
    }

    @Override
    public void act() {
        hearingPart = hearingPartMapper.mapToHearingPart(
            createHearingPartRequest,
            caseTypeRepository,
            hearingTypeRepository
        );

        hearingPart = hearingPartRepository.save(hearingPart);
    }

    @Override
    public void getAndValidateEntities() {
        // No op
    }

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
