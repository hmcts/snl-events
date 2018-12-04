package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.ActivityLoggable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.ActivityStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class AssignSessionsToHearingAction extends Action implements RulesProcessable, ActivityLoggable {

    protected static final String UPDATE_ACTION_TEXT = "update";
    protected final EntityManager entityManager;
    protected HearingSessionRelationship relationship;
    protected UUID hearingId;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> targetSessions;
    protected List<UUID> targetSessionsIds;
    protected List<String> previousHearingParts;
    protected String previousHearing;

    protected HearingRepository hearingRepository;
    protected SessionRepository sessionRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected Hearing savedHearing;

    @SuppressWarnings("squid:S00107") // we intentionally go around DI here as such the amount of parameters
    public AssignSessionsToHearingAction(UUID hearingId,
                                         HearingSessionRelationship relationship,
                                         HearingRepository hearingRepository,
                                         SessionRepository sessionRepository,
                                         StatusConfigService statusConfigService,
                                         StatusServiceManager statusServiceManager,
                                         EntityManager entityManager,
                                         ObjectMapper objectMapper) {
        this.relationship = relationship;
        this.hearingId = hearingId;
        this.hearingRepository = hearingRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(hearingId);

        if (hearing == null) {
            throw new SnlEventsException("Hearing cannot be null!");
        }

        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> statusServiceManager.canBeListed(hp))
            .collect(Collectors.toList());

        if (hearingParts == null || hearingParts.isEmpty()) {
            throw new SnlEventsException("Hearing parts cannot be null!");
        }

        if (!statusServiceManager.canBeListed(hearing)) {
            throw new SnlEventsException("Hearing can not be listed");
        }

        if (relationship.getSessionsData() == null) {
            throw new SnlEventsException("SessionsData cannot be null!");
        }

        targetSessionsIds = relationship.getSessionsData().stream()
            .map(SessionAssignmentData::getSessionId)
            .collect(Collectors.toList());

        targetSessions = sessionRepository.findSessionByIdIn(targetSessionsIds);
        if (targetSessions == null || targetSessions.isEmpty()) {
            throw new SnlEventsException("Target sessions cannot be null!");
        }

        if (targetSessions.size() != targetSessionsIds.size()) {
            throw new SnlEventsException("Number of sessions in DB is different then in request!");
        }
        // Missing a check for numberOfSessions returned from db against number of hearingParts(that can be used)
        // until a proper story will arise
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        final List<UUID> entitiesIds = new ArrayList<>();
        entitiesIds.add(hearing.getId());
        for (HearingPart hp : hearingParts) {
            entitiesIds.add(hp.getId());
            entitiesIds.add(hp.getSessionId());
        }
        entitiesIds.addAll(targetSessionsIds);

        return entitiesIds.toArray(new UUID[0]);
    }

    @Override
    public void act() {
        final StatusConfig listedConfig = statusConfigService.getStatusConfig(Status.Listed);
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);

            previousHearingParts = new ArrayList<>();
            AtomicInteger index = new AtomicInteger();
            for (HearingPart hp : hearingParts) {
                previousHearingParts.add(objectMapper.writeValueAsString(hp));

                Session session = targetSessions.get(index.getAndIncrement());
                hp.setSessionId(session.getId());
                hp.setSession(session);
                hp.setStatus(listedConfig);
                if (targetSessions.size() > 1) {
                    hp.setStart(session.getStart());
                } else {
                    hp.setStart(relationship.getStart());
                }
            }
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }
        entityManager.detach(hearing);

        hearing.setVersion(relationship.getHearingVersion());
        hearing.setStatus(listedConfig);
        savedHearing = hearingRepository.save(hearing);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
            savedHearing.getId(),
            null,
            "lock",
            "unlock",
            0)
        );

        userTransactionDataList.add(new UserTransactionData("hearing",
            savedHearing.getId(),
            previousHearing,
            UPDATE_ACTION_TEXT,
            UPDATE_ACTION_TEXT,
            1)
        );

        AtomicInteger index = new AtomicInteger();
        for (HearingPart hp : hearingParts) {
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                hp.getId(),
                previousHearingParts.get(index.getAndIncrement()),
                UPDATE_ACTION_TEXT,
                UPDATE_ACTION_TEXT,
                2
            ));
        }

        targetSessions.forEach(session ->
            userTransactionDataList.add(getLockedSessionTransactionData(session.getId()))
        );

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return relationship.getUserTransactionId();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        List<FactMessage> factsMessages = new ArrayList<>();
        for (HearingPart hp : hearingParts) {
            factsMessages.add(
                new FactMessage(RulesService.UPSERT_HEARING_PART, factsMapper.mapHearingToRuleJsonMessage(hp))
            );
        }
        return factsMessages;
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }

    @Override
    public List<ActivityLog> getActivities() {
        List activities = new ArrayList();

        ActivityLog activityLog = ActivityLog.builder()
            .userTransactionId(getUserTransactionId())
            .id(UUID.randomUUID())
            .entityId(relationship.getHearingId())
            .entityName(HEARING_ENTITY)
            .status(ActivityStatus.Listed)
            .build();

        activities.add(activityLog);

        return activities;
    }
}
