package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

public class DeleteListingRequestAction extends Action implements RulesProcessable {
    private DeleteListingRequest deleteListingRequest;
    private HearingRepository hearingRepository;
    private EntityManager entityManager;
    private ObjectMapper objectMapper;
    private Hearing hearing;
    private String currentHearingPartAsString;

    public DeleteListingRequestAction(
        DeleteListingRequest deleteListingRequest,
        HearingRepository hearingRepository,
        EntityManager entityManager,
        ObjectMapper objectMapper
    ) {
        this.deleteListingRequest = deleteListingRequest;
        this.hearingRepository = hearingRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(deleteListingRequest.getHearingId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing part not found");
        }
    }

    @Override
    public void act() {
        try {
            currentHearingPartAsString = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        entityManager.detach(hearing);
        hearing.setVersion(deleteListingRequest.getHearingVersion());
        hearing.setDeleted(true);

        hearingRepository.save(hearing);
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg = null;

        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.DELETE_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
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
        return new UUID[]{deleteListingRequest.getHearingId()};
    }
}
