package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected EntityManager entityManager;

    @SuppressWarnings("squid:S00107") // we intentionally go around DI here as such the amount of parameters
    public CreateListingRequestAction(CreateHearingRequest createHearingRequest,
                                      HearingMapper hearingMapper,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository,
                                      HearingRepository hearingRepository,
                                      StatusConfigService statusConfigService,
                                      StatusServiceManager statusServiceManager,
                                      EntityManager entityManager) {
        this.createHearingRequest = createHearingRequest;
        this.hearingMapper = hearingMapper;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.hearingRepository = hearingRepository;
        this.entityManager = entityManager;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
    }

    @Override
    @Transactional
    public void act() {
        final StatusConfig unlistedStatus = statusConfigService.getStatusConfig(Status.Unlisted);
        hearing.setStatus(unlistedStatus);

        hearingParts.forEach(hearingPart -> {
            hearingPart.setStatus(unlistedStatus);
            hearing.addHearingPart(hearingPart);
        });

        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        hearingParts = hearingMapper.mapToHearingParts(createHearingRequest);

        hearing = hearingMapper.mapToHearing(
            createHearingRequest,
            caseTypeRepository,
            hearingTypeRepository,
            entityManager
        );
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return hearing.getHearingParts().stream().map(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        hearingParts.forEach(hp ->
            userTransactionDataList.add(prepareCreateUserTransactionData("hearingPart", hp.getId(), 0))
        );
        userTransactionDataList.add(prepareCreateUserTransactionData("hearing", hearing.getId(), 1));

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
