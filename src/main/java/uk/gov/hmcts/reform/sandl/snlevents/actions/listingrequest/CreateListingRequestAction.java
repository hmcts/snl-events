package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

public class CreateListingRequestAction extends Action implements RulesProcessable {

    protected CreateHearingRequest createHearingRequest;
    protected List<HearingPart> hearingParts;
    protected Hearing hearing;

    protected HearingTypeRepository hearingTypeRepository;
    protected CaseTypeRepository caseTypeRepository;
    protected HearingMapper hearingMapper;
    protected HearingRepository hearingRepository;
    private EntityManager entityManager;

    public CreateListingRequestAction(CreateHearingRequest createHearingRequest,
                                      HearingMapper hearingMapper,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository,
                                      HearingRepository hearingRepository,
                                      EntityManager entityManager) {
        this.createHearingRequest = createHearingRequest;
        this.hearingMapper = hearingMapper;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.hearingRepository = hearingRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void act() {
        hearingParts = hearingMapper.mapToHearingParts(createHearingRequest);

        hearing = hearingMapper.mapToHearing(
            createHearingRequest,
            caseTypeRepository,
            hearingTypeRepository,
            entityManager
        );

        hearingParts.forEach(hearingPart -> {
            hearing.addHearingPart(hearingPart);
        });

        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        // No op
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg;
        try {
            //TODO wait for KZ implementation of multipart handling
            msg = factsMapper.mapHearingToRuleJsonMessage(hearing);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        int orderNr;
        for (orderNr = 0; orderNr < hearingParts.size(); orderNr++) {
            userTransactionDataList.add(prepareCreateUserTransactionData(
                "hearingPart", hearingParts.get(orderNr).getId(), orderNr));
        }
        userTransactionDataList.add(prepareCreateUserTransactionData("hearing", hearing.getId(), orderNr));

        return userTransactionDataList;
    }

    private UserTransactionData prepareCreateUserTransactionData(String entity, UUID entityId, int actionOrder) {
        return new UserTransactionData(entity,
            entityId,
            null,
            "create",
            "delete",
            actionOrder);
    }

    @Override
    public UUID getUserTransactionId() {
        return this.createHearingRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {this.createHearingRequest.getId()};
    }
}
