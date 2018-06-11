package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HearingPartService {

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    public List<HearingPart> getAllHearingParts() {
        return hearingPartRepository.findAll();
    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }

    @Transactional
    public UserTransaction saveWithTransaction(HearingPart hearingPart, UUID transactionId) {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPart);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearingPart", savedHearingPart.getId(), null, "update", "update", 0));

        return userTransactionService.startTransaction(transactionId, userTransactionDataList);
    }

    public UserTransaction assignHearingPartToSession(UUID hearingPartId,
                                                  HearingPartSessionRelationship assignment) throws IOException {
        HearingPart hearingPart = hearingPartRepository.findOne(hearingPartId);

        Session targetSession = (assignment.getSessionId() == null) ? null :
            sessionRepository.findOne(assignment.getSessionId());

        return userTransactionService.isBeingTransacted(assignment.getSessionId()) ?
                this.transactionCancelled(assignment.getUserTransactionId()) :
                this.assignHearingPartToSession(hearingPart, targetSession, assignment);
    }

    private UserTransaction transactionCancelled(UUID transactionId) {
        return new UserTransaction(transactionId,
                UserTransactionStatus.CANCELLED,
                UserTransactionRulesProcessingStatus.NOT_STARTED);
    }

    private UserTransaction assignHearingPartToSession(HearingPart hearingPart,
                                                       Session targetSession,
                                                       HearingPartSessionRelationship assignment) throws IOException {
        UUID targetSessionId = (targetSession == null) ? null : targetSession.getId();

        hearingPart.setSession(targetSession);
        hearingPart.setSessionId(targetSessionId);
        hearingPart.setStart(assignment.getStart());

        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        UserTransaction ut = saveWithTransaction(hearingPart, assignment.getUserTransactionId());
        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);

        return userTransactionService.rulesProcessed(ut);
    }

}