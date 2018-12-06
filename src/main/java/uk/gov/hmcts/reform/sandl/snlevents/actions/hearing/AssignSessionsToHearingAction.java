package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class AssignSessionsToHearingAction extends Action implements RulesProcessable {
    protected final EntityManager entityManager;
    protected HearingSessionRelationship relationship;
    protected UUID hearingId;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> targetSessions;
    protected List<UUID> targetSessionsIds;
    // id & HEARING part string
    protected Map<UUID, String> originalHearingParts;
    protected String previousHearing;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;
    protected SessionRepository sessionRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected Hearing savedHearing;
    protected UserTransactionDataPreparerService dataPreparerServce = new UserTransactionDataPreparerService();

    @SuppressWarnings("squid:S00107") // we intentionally go around DI here as such the amount of parameters
    public AssignSessionsToHearingAction(UUID hearingId,
                                         HearingSessionRelationship relationship,
                                         HearingRepository hearingRepository,
                                         HearingPartRepository hearingPartRepository,
                                         SessionRepository sessionRepository,
                                         StatusConfigService statusConfigService,
                                         StatusServiceManager statusServiceManager,
                                         EntityManager entityManager,
                                         ObjectMapper objectMapper) {
        this.relationship = relationship;
        this.hearingId = hearingId;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
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
            throw new SnlEventsException("Sessions Data cannot be null!");
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
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }
        final StatusConfig listedConfig = statusConfigService.getStatusConfig(Status.Listed);
        entityManager.detach(hearing);
        hearing.setVersion(relationship.getHearingVersion());
        hearing.setStatus(listedConfig);
        savedHearing = hearingRepository.save(hearing);

        originalHearingParts = dataPreparerServce.mapHearingPartsToStrings(objectMapper, hearingParts);
        AtomicInteger index = new AtomicInteger();
        for (HearingPart hp : hearingParts) {
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

        hearingPartRepository.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        originalHearingParts.forEach((id, hpString) ->
            dataPreparerServce.prepareUserTransactionDataForUpdate(UserTransactionDataPreparerService.HEARING_PART, id,
                hpString,  2)
        );

        dataPreparerServce.prepareLockedEntityTransactionData(UserTransactionDataPreparerService.HEARING,
            savedHearing.getId(), 0);
        dataPreparerServce.prepareUserTransactionDataForUpdate(UserTransactionDataPreparerService.HEARING,
            hearing.getId(), previousHearing, 1);

        targetSessions.forEach(s ->
            dataPreparerServce.prepareLockedEntityTransactionData(UserTransactionDataPreparerService.SESSION,
                s.getId(), 0)
        );

        return dataPreparerServce.getUserTransactionDataList();
    }

    @Override
    public UUID getUserTransactionId() {
        return relationship.getUserTransactionId();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return dataPreparerServce.generateUpsertHearingPartFactMsg(hearingParts, factsMapper);
    }
}
