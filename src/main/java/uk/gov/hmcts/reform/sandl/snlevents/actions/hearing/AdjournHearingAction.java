package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers.ActivityBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.ActivityLoggable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AdjournHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

public class AdjournHearingAction extends Action implements RulesProcessable, ActivityLoggable {
    protected AdjournHearingRequest adjournHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected EntityManager entityManager;

    // id & hearing part string
    private Map<UUID, String> originalHearingParts;
    private String previousHearing;
    private UserTransactionDataPreparerService dataPreparer = new UserTransactionDataPreparerService();

    public AdjournHearingAction(
        AdjournHearingRequest adjournHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper,
        EntityManager entityManager
    ) {
        this.adjournHearingRequest = adjournHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(adjournHearingRequest.getHearingId());
        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> statusServiceManager.canHearingPartBeAdjourned(hp))
            .collect(Collectors.toList());
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!statusServiceManager.canBeAdjourned(hearing)) {
            throw new SnlEventsException("Hearing can not be adjourned");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearingParts.stream().map(HearingPart::getId).collect(Collectors.toList());
        ids.addAll(sessions.stream().map(Session::getId).collect(Collectors.toList()));
        ids.add(adjournHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }

        hearing.setStatus(statusConfigService.getStatusConfig(Status.Adjourned));
        entityManager.detach(hearing);
        hearing.setVersion(adjournHearingRequest.getHearingVersion());
        hearingRepository.save(hearing);

        originalHearingParts = dataPreparer.mapHearingPartsToStrings(objectMapper, hearingParts);
        hearingParts.stream().forEach(hp -> {
            if (hp.getStart().isAfter(OffsetDateTime.now())) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Vacated));
                hp.setSessionId(null);
                hp.setSession(null);
            }
        });

        hearingPartRepository.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        originalHearingParts.forEach((id, hpString) ->
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                id,
                hpString,
                "update",
                "update",
                0)
            )
        );

        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
            previousHearing,
            "update",
            "update",
            1)
        );

        sessions.stream().forEach(s ->
            userTransactionDataList.add(prepareLockedEntityTransactionData("session", s.getId()))
        );

        return userTransactionDataList;
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return dataPreparer.generateDeleteHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public UUID getUserTransactionId() {
        return adjournHearingRequest.getUserTransactionId();
    }


    private UserTransactionData prepareLockedEntityTransactionData(String entity, UUID id) {
        return new UserTransactionData(entity, id, null, "lock", "unlock", 0);
    }

    @Override
    public List<ActivityLog> getActivities() {
        return ActivityBuilder.activityBuilder()
            .userTransactionId(getUserTransactionId())
            .withActivity(hearing.getId(), Hearing.ENTITY_NAME, ActivityStatus.Adjourned,
                adjournHearingRequest.getDescription())
            .build();
    }
}
