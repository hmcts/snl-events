package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
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
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

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
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected EntityManager entityManager;
    private UserTransactionDataPreparerService dataPrepService = new UserTransactionDataPreparerService();

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
        hearing = hearingMapper.mapToHearing(
            createHearingRequest,
            caseTypeRepository,
            hearingTypeRepository,
            entityManager
        );

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

        if (createHearingRequest.isMultiSession() && createHearingRequest.getNumberOfSessions() < 2) {
            throw new SnlEventsException("Multi-session hearings cannot have less than 2 sessions!");
        }

        if (!createHearingRequest.isMultiSession() && createHearingRequest.getNumberOfSessions() > 1) {
            throw new SnlEventsException("Single-session hearings cannot have more than 2 sessions!");
        }
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return dataPrepService.generateUpsertHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        hearingParts.forEach(hp ->
            dataPrepService.prepareUserTransactionDataForCreate("hearingPart", hp.getId(), 0)
        );

        dataPrepService.prepareUserTransactionDataForCreate("hearing", hearing.getId(), 1);

        return dataPrepService.getUserTransactionDataList();
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
