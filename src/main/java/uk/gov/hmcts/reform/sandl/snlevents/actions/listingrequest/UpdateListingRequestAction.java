package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateListingRequestAction extends Action implements RulesProcessable {

    private UpdateHearingPart updateHearingPart;
    private HearingPartRepository hearingPartRepository;
    private HearingPart hearingPart;
    private String currentHearingPartAsString;
    private EntityManager entityManager;

    public UpdateListingRequestAction(UpdateHearingPart updateHearingPart,
                                      HearingPartRepository hearingPartRepository,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper) {
        this.updateHearingPart = updateHearingPart;
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

        hearingPart.setId(updateHearingPart.getId());
        hearingPart.setCaseNumber(updateHearingPart.getCaseNumber());
        hearingPart.setCaseTitle(updateHearingPart.getCaseTitle());
        hearingPart.setCaseType(updateHearingPart.getCaseType());
        hearingPart.setHearingType(updateHearingPart.getHearingType());
        hearingPart.setDuration(updateHearingPart.getDuration());
        hearingPart.setScheduleStart(updateHearingPart.getScheduleStart());
        hearingPart.setScheduleEnd(updateHearingPart.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(updateHearingPart.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(updateHearingPart.getReservedJudgeId());

        if (updateHearingPart.getCreatedAt() == null) {
            hearingPart.setCreatedAt(OffsetDateTime.now());
        } else {
            hearingPart.setCreatedAt(updateHearingPart.getCreatedAt());
        }
        hearingPart.setPriority(updateHearingPart.getPriority());

        entityManager.detach(hearingPart);
        hearingPart.setVersion(updateHearingPart.getVersion());
        hearingPartRepository.save(hearingPart);
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(updateHearingPart.getId());

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
        try {
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                hearingPart.getId(),
                currentHearingPartAsString,
                "update",
                "update",
                0)
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return updateHearingPart.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {updateHearingPart.getId()};
    }
}
