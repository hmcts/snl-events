package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

public class UpdateListingRequestAction extends Action implements RulesProcessable {

    private UpdateListingRequest updateListingRequest;
    private HearingPartRepository hearingPartRepository;
    private HearingPart hearingPart;
    private String currentHearingPartAsString;
    private EntityManager entityManager;

    public UpdateListingRequestAction(UpdateListingRequest updateListingRequest,
                                      HearingPartRepository hearingPartRepository,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper) {
        this.updateListingRequest = updateListingRequest;
        this.hearingPartRepository = hearingPartRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void act() {
        try {
            currentHearingPartAsString = objectMapper.writeValueAsString(hearingPart);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        hearingPart.setId(updateListingRequest.getId());
        hearingPart.setCaseNumber(updateListingRequest.getCaseNumber());
        hearingPart.setCaseTitle(updateListingRequest.getCaseTitle());
        hearingPart.setCaseType(updateListingRequest.getCaseType());
        hearingPart.setHearingType(updateListingRequest.getHearingType());
        hearingPart.setDuration(updateListingRequest.getDuration());
        hearingPart.setScheduleStart(updateListingRequest.getScheduleStart());
        hearingPart.setScheduleEnd(updateListingRequest.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(updateListingRequest.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(updateListingRequest.getReservedJudgeId());

        if (updateListingRequest.getCreatedAt() == null) {
            hearingPart.setCreatedAt(OffsetDateTime.now());
        } else {
            hearingPart.setCreatedAt(updateListingRequest.getCreatedAt());
        }
        hearingPart.setPriority(updateListingRequest.getPriority());

        entityManager.detach(hearingPart);
        hearingPart.setVersion(updateListingRequest.getVersion());
        hearingPartRepository.save(hearingPart);
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(updateListingRequest.getId());

        if (hearingPart == null) {
            throw new EntityNotFoundException("Hearing part not found");
        }
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg = null;
        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearingPart",
            hearingPart.getId(),
            currentHearingPartAsString,
            "update",
            "update",
            0)
        );
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
