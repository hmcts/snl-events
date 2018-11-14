package uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

public class AssignHearingPartToSessionAction extends Action implements RulesProcessable {

    protected HearingPartSessionRelationship relationship;
    protected UUID hearingPartId;
    protected HearingPart hearingPart;
    protected Session targetSession;
    protected String previousHearingPart;
    protected String previousHearing;
    protected Session previousSession;

    protected HearingPartRepository hearingPartRepository;
    protected EntityManager entityManager;
    protected SessionRepository sessionRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;

    @SuppressWarnings("squid:S00107") // we intentionally go around DI here as such the amount of parameters
    public AssignHearingPartToSessionAction(UUID hearingPartId,
                                            HearingPartSessionRelationship hearingPartSessionRelationship,
                                            HearingPartRepository hearingPartRepository,
                                            SessionRepository sessionRepository,
                                            StatusConfigService statusConfigService,
                                            StatusServiceManager statusServiceManager,
                                            EntityManager entityManager,
                                            ObjectMapper objectMapper) {
        this.relationship = hearingPartSessionRelationship;
        this.hearingPartId = hearingPartId;
        this.hearingPartRepository = hearingPartRepository;
        this.sessionRepository = sessionRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(hearingPartId);
        if (hearingPart == null) {
            throw new SnlEventsException("Hearing part cannot be null!");
        }
        // this below might be wrong, but the thinking behind is that it has to be first unlisted and the listed again
        if (!statusServiceManager.canBeUnlisted(hearingPart)) {
            throw new SnlEventsException("Hearing part can not be unlisted");
        }

        targetSession = sessionRepository.findOne(relationship.getSessionData().getSessionId());
        if (targetSession == null) {
            throw new SnlEventsException("Target sessions cannot be null!");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        // below, because Arrays.asList returns AbstractList which does not support add method
        List<UUID> entitiesIds = new ArrayList<>(Arrays.asList(
            hearingPart.getId(), hearingPart.getHearingId(), targetSession.getId()
        ));
        if (hearingPart.getSessionId() != null) {
            entitiesIds.add(hearingPart.getSessionId());
        }
        return entitiesIds.toArray(new UUID[0]);
    }

    @Override
    public void act() {
        try {
            previousHearingPart = objectMapper.writeValueAsString(hearingPart);
            previousHearing = objectMapper.writeValueAsString(hearingPart.getHearing());
            previousSession = hearingPart.getSession();
        } catch (JsonProcessingException e) {
            throw new SnlEventsException(e);
        }
        entityManager.detach(hearingPart);
        hearingPart.setVersion(relationship.getHearingPartVersion());

        UUID targetSessionId = (targetSession == null) ? null : targetSession.getId();
        hearingPart.setSessionId(targetSessionId);
        hearingPart.setSession(targetSession);
        hearingPart.setStatus(statusConfigService.getStatusConfig(Status.Listed));

    }

    @Override //Done although hearing and session for user transactionDAta are not needed
    public List<UserTransactionData> generateUserTransactionData() {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPart);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.add(new UserTransactionData("hearing",
            savedHearingPart.getHearingId(),
            previousHearing,
            "lock",
            "unlock",
            0)
        );

        userTransactionDataList.add(new UserTransactionData("hearingPart",
            savedHearingPart.getId(),
            previousHearingPart,
            "update",
            "update",
            1)
        );

        if (previousSession != null) {
            userTransactionDataList.add(getLockedSessionTransactionData(previousSession.getId()));
        }
        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));

        return userTransactionDataList;
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        String msg;
        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new SnlEventsException(e);
        }
        return Collections.singletonList(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
    }

    @Override
    public UUID getUserTransactionId() {
        return relationship.getUserTransactionId();
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }

}
