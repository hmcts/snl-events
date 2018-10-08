package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

public class DeleteListingRequestAction extends Action implements RulesProcessable {
    private DeleteListingRequest deleteListingRequest;
    private HearingPartRepository hearingPartRepository;
    private EntityManager entityManager;
    private ObjectMapper objectMapper;
    private HearingPart hearingPart;
    private String currentHearingPartAsString;

    public DeleteListingRequestAction(
        DeleteListingRequest deleteListingRequest,
        HearingPartRepository hearingPartRepository,
        EntityManager entityManager,
        ObjectMapper objectMapper
    ) {
        this.deleteListingRequest = deleteListingRequest;
        this.hearingPartRepository = hearingPartRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(deleteListingRequest.getHearingPartId());

        if (hearingPart == null) {
            throw new EntityNotFoundException("Hearing part not found");
        }
    }

    @Override
    public void act() {
        try {
            currentHearingPartAsString = objectMapper.writeValueAsString(hearingPart);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        entityManager.detach(hearingPart);
        hearingPart.setVersion(deleteListingRequest.getHearingPartVersion());
        hearingPart.setDeleted(true);

        hearingPartRepository.save(hearingPart);
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg;

        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.DELETE_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearingPart",
            hearingPart.getId(),
            currentHearingPartAsString,
            "delete",
            "create",
            0)
        );

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return deleteListingRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[]{deleteListingRequest.getHearingPartId()};
    }
}
