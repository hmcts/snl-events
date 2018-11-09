package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class AssignSessionsToHearingAction extends Action implements RulesProcessable {

    protected final EntityManager entityManager;
    protected HearingSessionRelationship relationship;
    protected UUID hearingId;
    protected Hearing hearing;
    protected List<Session> targetSessions;
    protected List<UUID> targetSessionsIds;
    protected List<String> previousHearingParts;
    protected String previousHearing;

    protected HearingRepository hearingRepository;
    protected SessionRepository sessionRepository;
    protected Hearing savedHearing;

    public AssignSessionsToHearingAction(UUID hearingId,
                                         HearingSessionRelationship relationship,
                                         HearingRepository hearingRepository,
                                         SessionRepository sessionRepository,
                                         EntityManager entityManager,
                                         ObjectMapper objectMapper) {
        this.relationship = relationship;
        this.hearingId = hearingId;
        this.hearingRepository = hearingRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(hearingId);
        if (hearing == null || hearing.getHearingParts() == null || hearing.getHearingParts().size() == 0) {
            throw new SnlEventsException("Hearing cannot be null!");
        }

        if (relationship.getSessionsData() == null) {
            throw new SnlEventsException("SessionsData cannot be null!");
        }
        targetSessionsIds = relationship.getSessionsData().stream()
            .map(SessionAssignmentData::getSessionId)
            .collect(Collectors.toList());

        targetSessions = sessionRepository.findSessionByIdIn(targetSessionsIds);
        if (targetSessions == null || targetSessions.size() == 0) {
            throw new SnlEventsException("Target sessions cannot be null!");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        final List<UUID> entitiesIds = new ArrayList<>();
        entitiesIds.add(hearing.getId());
        for (HearingPart hp : hearing.getHearingParts()) {
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
            entityManager.detach(hearing);

            hearing.setVersion(relationship.getHearingVersion());

            previousHearingParts = new ArrayList<>();
            AtomicInteger index = new AtomicInteger();
            for (HearingPart hp : hearing.getHearingParts()) {
                previousHearingParts.add(objectMapper.writeValueAsString(hp));

                Session session = targetSessions.get(index.getAndIncrement());
                hp.setSessionId(session.getId());
                hp.setSession(session);
                if (targetSessions.size() > 1) {
                    hp.setStart(session.getStart());
                } else {
                    hp.setStart(relationship.getStart());
                }
            }
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }
        savedHearing = hearingRepository.save(hearing);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
            savedHearing.getId(),
            previousHearing,
            "lock",
            "unlock",
            0)
        );

        AtomicInteger index = new AtomicInteger();
        for (HearingPart hp : hearing.getHearingParts()) {
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                hp.getId(),
                previousHearingParts.get(index.getAndIncrement()),
                "update",
                "update",
                1
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
        for (HearingPart hp : hearing.getHearingParts()) {
            factsMessages.add(
                new FactMessage(RulesService.UPSERT_HEARING_PART, factsMapper.mapHearingToRuleJsonMessage(hp))
            );
        }
        return factsMessages;
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }
}
