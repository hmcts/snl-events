package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

public class UpdateListingRequestAction extends Action implements RulesProcessable {

    private UpdateListingRequest updateListingRequest;
    private String currentHearingAsString;
    private EntityManager entityManager;
    private HearingTypeRepository hearingTypeRepository;
    private CaseTypeRepository caseTypeRepository;
    private Hearing hearing;
    private HearingRepository hearingRepository;

    public UpdateListingRequestAction(UpdateListingRequest updateListingRequest,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository,
                                      HearingRepository hearingRepository) {
        this.updateListingRequest = updateListingRequest;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.hearingRepository = hearingRepository;
    }

    @Override
    public void act() {
        try {
            currentHearingAsString = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        entityManager.detach(hearing);

        hearing.setCaseNumber(updateListingRequest.getCaseNumber());
        hearing.setCaseTitle(updateListingRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(updateListingRequest.getCaseTypeCode());
        hearing.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(updateListingRequest.getHearingTypeCode());
        hearing.setHearingType(hearingType);
        hearing.setDuration(updateListingRequest.getDuration());
        hearing.setScheduleStart(updateListingRequest.getScheduleStart());
        hearing.setScheduleEnd(updateListingRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(updateListingRequest.getCommunicationFacilitator());
        hearing.setReservedJudge(
            this.entityManager.getReference(Person.class, updateListingRequest.getReservedJudgeId())
        );
        hearing.setPriority(updateListingRequest.getPriority());
        hearing.setVersion(updateListingRequest.getVersion());
        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(updateListingRequest.getId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg = null;
        try {
            msg = factsMapper.mapHearingToRuleJsonMessage(hearing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
            currentHearingAsString,
            "update",
            "update",
            0));

        hearing.getHearingParts().forEach(hp -> userTransactionDataList.add(new UserTransactionData("hearingPart",
            hp.getId(),
            null,
            "lock",
            "unlock",
            0)));

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return updateListingRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {updateListingRequest.getId()};
    }
}
