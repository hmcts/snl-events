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
import uk.gov.hmcts.reform.sandl.snlevents.model.request.WithdrawHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class WithdrawHearingAction extends Action implements RulesProcessable, ActivityLoggable {
    protected WithdrawHearingRequest withdrawHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected EntityManager entityMngr;
    protected StatusConfigService statusConfigService;
    protected HearingPartRepository hearingPartRepo;
    protected StatusServiceManager statusServiceManager;

    // id & HEARING part string
    private Map<UUID, String> originalHearingParts;
    private String previousHearing;
    private UserTransactionDataPreparerService utdps = new UserTransactionDataPreparerService();

    public WithdrawHearingAction(
        WithdrawHearingRequest withdrawHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepo,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objMapper,
        EntityManager entityMngr
    ) {
        this.withdrawHearingRequest = withdrawHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepo = hearingPartRepo;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objMapper;
        this.entityMngr = entityMngr;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(withdrawHearingRequest.getHearingId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> statusServiceManager.canBeWithdrawn(hp))
            .collect(Collectors.toList());

        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        // Validation moved to act() due to conflict with optimistic lock

        if (!statusServiceManager.canBeWithdrawn(hearing)) {
            throw new SnlEventsException("Hearing can not be withdrawn");
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

        ids.add(withdrawHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }

        entityMngr.detach(hearing);
        hearing.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));
        hearing.setVersion(withdrawHearingRequest.getHearingVersion());
        hearingRepository.save(hearing);

        originalHearingParts = utdps.mapHearingPartsToStrings(objectMapper, hearingParts);
        hearingParts.forEach(hp -> {
            hp.setSession(null);
            hp.setStart(null);
            if (hp.getStatus().getStatus() == Status.Listed) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Vacated));
            } else if (hp.getStatus().getStatus() == Status.Unlisted) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));
                hearing.setNumberOfSessions(hearing.getNumberOfSessions() - 1);
            }
        });

        hearingPartRepo.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        originalHearingParts.forEach((id, hpString) ->
            utdps.prepareUserTransactionDataForUpdate(UserTransactionDataPreparerService.HEARING_PART, id, hpString,2)
        );

        utdps.prepareUserTransactionDataForUpdate(UserTransactionDataPreparerService.HEARING, hearing.getId(),
            previousHearing, 1);

        sessions.forEach(s ->
            utdps.prepareLockedEntityTransactionData(UserTransactionDataPreparerService.SESSION, s.getId(), 0)
        );

        return utdps.getUserTransactionDataList();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return utdps.generateDeleteHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public UUID getUserTransactionId() {
        return withdrawHearingRequest.getUserTransactionId();
    }

    @Override
    public List<ActivityLog> getActivities() {
        return ActivityBuilder.activityBuilder()
            .userTransactionId(getUserTransactionId())
            .withActivity(hearing.getId(), Hearing.ENTITY_NAME, ActivityStatus.Withdrawn)
            .build();
    }
}
