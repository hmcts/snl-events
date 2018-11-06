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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

public class DeleteListingRequestAction extends Action implements RulesProcessable {
    private DeleteListingRequest deleteListingRequest;
    private HearingRepository hearingRepository;
    private EntityManager entityManager;
    private Hearing hearing;
    private String currentHearingAsString;
    private HashMap<UUID, String> currentHearingPartsMap = new HashMap<>();

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
            throw new EntityNotFoundException("Hearing not found");
        }
    }

    @Override
    public void act() {
        currentHearingAsString = writeObjectAsString(hearing);

        hearing.getHearingParts().forEach(hp ->
            currentHearingPartsMap.put(hp.getId(), writeObjectAsString(hp)));

        entityManager.detach(hearing);
        hearing.setVersion(deleteListingRequest.getHearingVersion());
        hearing.setDeleted(true);
        hearing.getHearingParts().forEach(hp -> hp.setDeleted(true));

        hearingRepository.save(hearing);
    }

    private String writeObjectAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return hearing.getHearingParts().stream().map(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            return new FactMessage(RulesService.DELETE_HEARING_PART, msg);
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(prepareDeleteUserTransactionData("hearing", hearing.getId(),
            currentHearingAsString));

        currentHearingPartsMap.forEach((id, jsonValue) ->
            userTransactionDataList.add(prepareDeleteUserTransactionData("hearingPart", id, jsonValue)));

        return userTransactionDataList;
    }

    private UserTransactionData prepareDeleteUserTransactionData(String entityName, UUID entityId,
                                                                 String currentEntityString) {
        return new UserTransactionData(entityName,
            entityId,
            currentEntityString,
            "delete",
            "create",
            0);
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
