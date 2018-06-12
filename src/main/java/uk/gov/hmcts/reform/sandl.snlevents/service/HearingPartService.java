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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

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
        userTransactionDataList.add(new UserTransactionData("hearingPart",
                savedHearingPart.getId(),
                null,
                "update",
                "update",
                0)
        );

        return userTransactionService.startTransaction(transactionId, userTransactionDataList);
    }

    public UserTransaction assignHearingPartToSession(UUID hearingPartId,
                                                  HearingPartSessionRelationship assignment) throws IOException {
        HearingPart hearingPart = hearingPartRepository.findOne(hearingPartId);

        Session targetSession = sessionRepository.findOne(assignment.getSessionId());

        return targetSession == null || areTransactionsInProgress(hearingPart, assignment)
                ? transactionConflicted(assignment.getUserTransactionId())
                : assignHearingPartToSession(hearingPart, targetSession, assignment);
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

        ut = userTransactionService.rulesProcessed(ut);
        return userTransactionService.commit(ut.getId());
    }

    private UserTransaction transactionConflicted(UUID transactionId) {
        return new UserTransaction(transactionId,
                UserTransactionStatus.CONFLICT,
                UserTransactionRulesProcessingStatus.NOT_STARTED);
    }

    private boolean areTransactionsInProgress(HearingPart hearingPart, HearingPartSessionRelationship assignment) {
        return userTransactionService.isAnyBeingTransacted(hearingPart.getId(),
                hearingPart.getSessionId(),
                assignment.getSessionId());
    }
}