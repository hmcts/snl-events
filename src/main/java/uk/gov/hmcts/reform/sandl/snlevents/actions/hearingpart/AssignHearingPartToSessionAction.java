package uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssignHearingPartToSessionAction extends Action implements RulesProcessable {

    protected HearingPartSessionRelationship hearingPartSessionRelationship;
    protected UUID hearingPartId;
    protected HearingPart hearingPart;
    protected Session targetSession;

    protected HearingPartRepository hearingPartRepository;

    protected SessionRepository sessionRepository;

    public AssignHearingPartToSessionAction(UUID hearingPartId,
                                            HearingPartSessionRelationship hearingPartSessionRelationship,
                                            HearingPartRepository hearingPartRepository,
                                            SessionRepository sessionRepository) {
        this.hearingPartSessionRelationship = hearingPartSessionRelationship;
        this.hearingPartId = hearingPartId;
        this.hearingPartRepository = hearingPartRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(hearingPartId);
        targetSession = sessionRepository.findOne(hearingPartSessionRelationship.getSessionId());
        if (targetSession == null) {
            throw new RuntimeException("Target session cannot be null!");
        } else if (hearingPart == null) {
            throw new RuntimeException("Hearing part cannot be null!");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {hearingPart.getId(), hearingPart.getSessionId(), targetSession.getId()};
    }

    @Override
    public void act() {
        hearingPart.setSession(targetSession);
        hearingPart.setSessionId(targetSession.getId());
        hearingPart.setStart(hearingPartSessionRelationship.getStart());

        hearingPartRepository.save(hearingPart);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        try {
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                hearingPart.getId(),
                objectMapper.writeValueAsString(hearingPart),
                "update",
                "update",
                0)
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (hearingPart.getSession() != null) {
            userTransactionDataList.add(getLockedSessionTransactionData(hearingPart.getSession().getId()));
        }
        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));

        return userTransactionDataList;
    }

    @Override
    public FactMessage generateFactMessage() {
        String msg = null;
        try {
            msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public UUID getUserTransactionId() {
        return hearingPartSessionRelationship.getUserTransactionId();
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
    }
}
