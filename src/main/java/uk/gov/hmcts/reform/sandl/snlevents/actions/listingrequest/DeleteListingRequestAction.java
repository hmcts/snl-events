package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

public class DeleteListingRequestAction extends Action implements RulesProcessable {
    private DeleteListingRequest deleteListingRequest;
    private HearingRepository hearingRepository;
    private HearingPartRepository hearingPartRepository;
    private EntityManager entityManager;
    private Hearing hearing;
    private String currentHearingAsString;
    private List<HearingPart> hearingParts;
    private HashMap<UUID, String> currentHearingPartsMap = new HashMap<>();
    private UserTransactionDataPreparerService tdps = new UserTransactionDataPreparerService();

    public DeleteListingRequestAction(
        DeleteListingRequest deleteListingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        EntityManager entityManager,
        ObjectMapper objectMapper
    ) {
        this.deleteListingRequest = deleteListingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(deleteListingRequest.getHearingId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        hearingParts = hearing.getHearingParts();
    }

    @Override
    public void act() {
        currentHearingAsString = writeObjectAsString(hearing);
        entityManager.detach(hearing);
        hearing.setVersion(deleteListingRequest.getHearingVersion());
        hearing.setDeleted(true);
        hearingRepository.save(hearing);

        hearingParts.forEach(hp -> {
            hp.setDeleted(true);
            currentHearingPartsMap.put(hp.getId(), writeObjectAsString(hp));
        });
        
        hearingPartRepository.save(hearingParts);
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
        return tdps.generateDeleteHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        tdps.prepareUserTransactionDataForDelete(UserTransactionDataPreparerService.HEARING, hearing.getId(),
            currentHearingAsString, 0);

        currentHearingPartsMap.forEach((id, jsonValue) ->
            tdps.prepareUserTransactionDataForDelete(UserTransactionDataPreparerService.HEARING_PART, id, jsonValue, 1)
        );

        return tdps.getUserTransactionDataList();
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
