package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
public class HearingPartService {

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HearingTypeRepository hearingTypeRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private HearingMapper hearingMapper;
//
//    public HearingPartResponse createHearingPart(CreateHearingPartRequest createHearingPartRequest) throws IOException {
//        HearingPart hearingPart = hearingMapper.mapToHearingPart(
//            createHearingPartRequest,
//            caseTypeRepository,
//            hearingTypeRepository
//        );
//        hearingPart = save(hearingPart);
//        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
//        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);
//
//        return new HearingPartResponse(hearingPart);
//    }

    public List<HearingPartResponse> getAllHearingParts() {
        return hearingPartRepository
            .findAll()
            .stream()
            .map(HearingPartResponse::new)
            .collect(Collectors.toList());
    }

    public List<HearingPartResponse> getAllHearingPartsThat(Boolean areListed) {
        List<HearingPart> hearingParts;
        if (areListed) {
            hearingParts = hearingPartRepository.findBySessionIsNotNull();
        } else {
            hearingParts = hearingPartRepository.findBySessionIsNull();
        }

        return hearingParts.stream()
            .map(HearingPartResponse::new)
            .collect(Collectors.toList());
    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }

    public HearingPart findOne(UUID id) {
        return hearingPartRepository.findOne(id);
    }

    @Transactional
    public UserTransaction assignWithTransaction(Hearing hearing, UUID transactionId,
                                                 Session currentSession,
                                                 Session targetSession) throws JsonProcessingException {
        Hearing savedHearingPart = hearingRepository.save(hearing);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearingPart",
                savedHearingPart.getId(),
                objectMapper.writeValueAsString(hearing),
                "update",
                "update",
                0)
        );

        if (currentSession != null) {
            userTransactionDataList.add(getLockedSessionTransactionData(currentSession.getId()));
        }
        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));

        return userTransactionService.startTransaction(transactionId, userTransactionDataList);
    }

    public UserTransaction assignHearingPartToSessionWithTransaction(UUID hearingId,
                                                                     HearingPartSessionRelationship assignment)
                                                                        throws IOException {
        Hearing hearing = hearingRepository.findOne(hearingId);
        Session targetSession = sessionRepository.findOne(assignment.getSessionId());

        return targetSession == null || areTransactionsInProgress(hearing, assignment)
                ? userTransactionService.transactionConflicted(assignment.getUserTransactionId())
                : assignHearingPartToSessionWithTransaction(hearing, targetSession, assignment);
    }

    private UserTransaction assignHearingPartToSessionWithTransaction(Hearing hearing,
                                                                      Session targetSession,
                                                                      HearingPartSessionRelationship assignment)
                                                                        throws IOException {
        entityManager.detach(hearing);
        hearing.setVersion(assignment.getHearingPartVersion());
        HearingPart hearingPart = hearing.getHearingParts().get(0);

        UUID targetSessionId = (targetSession == null) ? null : targetSession.getId();
        hearingPart.setSessionId(targetSessionId);
        hearingPart.setSession(targetSession);
        hearing.setStart(assignment.getStart());

        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearing);
        UserTransaction ut = assignWithTransaction(hearing,
                assignment.getUserTransactionId(),
                hearingPart.getSession(),
                targetSession);
        rulesService.postMessage(assignment.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

        return userTransactionService.rulesProcessed(ut);
    }

    private boolean areTransactionsInProgress(Hearing hearing, HearingPartSessionRelationship assignment) {
        return userTransactionService.isAnyBeingTransacted(hearing.getId(),
                hearing.getHearingParts().get(0).getSessionId(),
                assignment.getSessionId());
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
