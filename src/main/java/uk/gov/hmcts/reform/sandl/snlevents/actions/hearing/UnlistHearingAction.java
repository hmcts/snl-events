package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers.ActivityBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.ActivityLoggable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.activities.ActivityStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class UnlistHearingAction extends Action implements RulesProcessable, ActivityLoggable {
    protected UnlistHearingRequest unlistHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hpRepo;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected EntityManager entityMgr;

    // id & HEARING part string
    private Map<UUID, String> originalHearingParts;
    private String previousHearing;
    private UserTransactionDataPreparerService userTransactionDataPreparerService =
        new UserTransactionDataPreparerService();

    public UnlistHearingAction(
        UnlistHearingRequest unlistHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hpRepo,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMap,
        EntityManager entityMgr
    ) {
        this.unlistHearingRequest = unlistHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hpRepo = hpRepo;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objectMap;
        this.entityMgr = entityMgr;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(unlistHearingRequest.getHearingId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> statusServiceManager.canBeUnlisted(hp))
            .collect(Collectors.toList());

        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        // Validation moved to act() due to conflict with optimistic lock

        if (!statusServiceManager.canBeUnlisted(hearing)) {
            throw new SnlEventsException("Hearing can not be unlisted");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearingParts
            .stream()
            .map(HearingPart::getId)
            .collect(Collectors.toList());

        ids.addAll(hearingParts
            .stream()
            .map(HearingPart::getSessionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));

        ids.add(unlistHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        if (sessions.isEmpty()) {
            throw new EntityNotFoundException("Hearing parts assigned to Hearing haven't been listed yet");
        }
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }

        entityMgr.detach(hearing);
        hearing.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
        hearingRepository.save(hearing);

        originalHearingParts = userTransactionDataPreparerService.mapHearingPartsToStrings(objectMapper, hearingParts);
        hearingParts.forEach(hp -> {
            hp.setSession(null);
            hp.setStart(null);
            VersionInfo vi = getVersionInfo(hp);
            hp.setVersion(vi.getVersion());
            hp.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
        });

        hpRepo.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        originalHearingParts.forEach((id, hpString) ->
            userTransactionDataPreparerService.prepareUserTransactionDataForUpdate(
                UserTransactionDataPreparerService.HEARING_PART, id, hpString,  2)
        );

        userTransactionDataPreparerService.prepareUserTransactionDataForUpdate(
            UserTransactionDataPreparerService.HEARING, hearing.getId(), previousHearing, 1);

        sessions.forEach(s ->
            userTransactionDataPreparerService.prepareLockedEntityTransactionData(
                UserTransactionDataPreparerService.SESSION, s.getId(), 0)
        );

        return userTransactionDataPreparerService.getUserTransactionDataList();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return userTransactionDataPreparerService.generateUpsertHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public UUID getUserTransactionId() {
        return unlistHearingRequest.getUserTransactionId();
    }

    private VersionInfo getVersionInfo(HearingPart hp) {
        Optional<VersionInfo> hpvi = unlistHearingRequest.getHearingPartsVersions()
            .stream()
            .filter(hpv -> hpv.getId().equals(hp.getId()))
            .findFirst();
        return hpvi.orElseThrow(() ->
            new EntityNotFoundException("Couldn't find version for HEARING part with id " + hp.getId().toString())
        );
    }

    @Override
    public List<ActivityLog> getActivities() {
        return ActivityBuilder.activityBuilder()
            .userTransactionId(getUserTransactionId())
            .withActivity(hearing.getId(), Hearing.ENTITY_NAME, ActivityStatus.Unlisted)
            .build();
    }
}
