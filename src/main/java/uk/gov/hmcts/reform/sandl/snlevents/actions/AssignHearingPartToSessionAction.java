package uk.gov.hmcts.reform.sandl.snlevents.actions;

import org.springframework.beans.factory.annotation.Autowired;
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

public class AssignHearingPartToSessionAction extends Action {

    protected HearingPartSessionRelationship hearingPartSessionRelationship;
    protected UUID hearingPartId;

    protected HearingPart hearingPart;
    protected Session targetSession;

    @Autowired
    protected HearingPartRepository hearingPartRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    public AssignHearingPartToSessionAction(UUID hearingPartId,
                                            HearingPartSessionRelationship hearingPartSessionRelationship) {
        this.hearingPartSessionRelationship = hearingPartSessionRelationship;
        this.hearingPartId = hearingPartId;
    }

    @Override
    public void initialize() {
        hearingPart = hearingPartRepository.findOne(hearingPartId);
        targetSession = sessionRepository.findOne(hearingPartSessionRelationship.getSessionId());
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() throws Exception {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearingPart",
            hearingPart.getId(),
            objectMapper.writeValueAsString(hearingPart),
            "update",
            "update",
            0)
        );

        if (hearingPart.getSession() != null) {
            userTransactionDataList.add(getLockedSessionTransactionData(hearingPart.getSession().getId()));
        }
        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));

        return userTransactionDataList;
    }

    @Override
    public FactMessage generateFactMessage() throws Exception {
        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);

        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
    }

    @Override
    public UUID getUserTransactionId() {
        return hearingPartSessionRelationship.getUserTransactionId();
    }

    @Override
    public void validate() throws Exception {
        if(targetSession == null) {
            throw new Exception("Target session cannot be null!");
        } else if(hearingPart == null) {
            throw new Exception("Hearing part cannot be null!");
        }
    }

    @Override
    public void act() {
        hearingPart.setSession(targetSession);
        hearingPart.setSessionId(targetSession.getId());
        hearingPart.setStart(hearingPartSessionRelationship.getStart());

        hearingPartRepository.save(hearingPart); // TODO: Extract into a separate 'persist' method?
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {hearingPart.getId(), hearingPart.getSessionId(), targetSession.getId()};
    }

    private UserTransactionData getLockedSessionTransactionData(UUID id) {
        return new UserTransactionData("session",
            id,
            null,
            "lock",
            "unlock",
            0);
    }

}
