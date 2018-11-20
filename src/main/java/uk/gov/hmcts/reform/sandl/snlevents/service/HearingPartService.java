package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

    public List<HearingPartResponse> getAllHearingParts() {
        return hearingPartRepository
            .findAll()
            .stream()
            .map(HearingPartResponse::new)
            .collect(Collectors.toList());
    }

    @Deprecated // This should be rewritten to native query instead of filtering in DB as part of SL-1971
    public List<HearingPartResponse> getAllHearingPartsThat(Boolean areListed) {
        List<HearingPart> hearingParts = hearingPartRepository.findAll();
        if (areListed) {
            return hearingParts.stream()
                .filter(hp -> {
                    StatusConfig statusConfig = hp.getHearing().getStatus();
                    return statusConfig.getStatus().equals(Status.Listed);
                })
                .map(HearingPartResponse::new)
                .collect(Collectors.toList());
        } else {
            return hearingParts.stream()
                .filter(hp -> {
                    StatusConfig statusConfig = hp.getHearing().getStatus();
                    return statusConfig.isCanBeListed() && !statusConfig.getStatus().equals(Status.Listed);
                })
                .map(HearingPartResponse::new)
                .collect(Collectors.toList());
        }


    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }

    public HearingPart findOne(UUID id) {
        return hearingPartRepository.findOne(id);
    }

    public UserTransaction assignWithTransaction(Hearing hearing, UUID transactionId,
                                                 List<Session> targetSessions, String beforeHearing,
                                                 List<String> previousHearingParts) {
        Hearing savedHearing = hearingRepository.save(hearing);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
                savedHearing.getId(),
                beforeHearing,
                "update",
                "update",
                0)
        );

        AtomicInteger index = new AtomicInteger();

        hearing.getHearingParts().forEach(hp ->
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                hp.getId(),
                previousHearingParts.get(index.getAndIncrement()),
                "update",
                "update",
                1
                ))
        );

        targetSessions.forEach(session ->
            userTransactionDataList.add(getLockedSessionTransactionData(session.getId()))
        );

        return userTransactionService.startTransaction(transactionId, userTransactionDataList);
    }

    @Transactional
    public UserTransaction assignWithTransaction(HearingPart hearingPart, UUID transactionId,
                                                 Session currentSession,
                                                 Session targetSession,
                                                 String previousHearingPart, String previousHearing) {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPart);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.add(new UserTransactionData("hearing",
            savedHearingPart.getHearingId(),
            previousHearing,
            "update",
            "update",
            0)
        );

        userTransactionDataList.add(new UserTransactionData("hearingPart",
            savedHearingPart.getId(),
            previousHearingPart,
            "update",
            "update",
            1)
        );

        if (currentSession != null) {
            userTransactionDataList.add(getLockedSessionTransactionData(currentSession.getId()));
        }
        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));

        return userTransactionService.startTransaction(transactionId, userTransactionDataList);
    }

    public UserTransaction assignHearingToSessionWithTransaction(UUID hearingId,
                                                                 HearingSessionRelationship assignment)
                                                                        throws IOException {
        Hearing hearing = hearingRepository.findOne(hearingId);
        List<UUID> sessionIds = assignment.getSessionsData()
            .stream()
            .map(SessionAssignmentData::getSessionId)
            .collect(Collectors.toList());

        List<Session> targetSessions = sessionRepository.findSessionByIdIn(sessionIds);

        return targetSessions.isEmpty() || areTransactionsInProgress(hearing, assignment)
                ? userTransactionService.transactionConflicted(assignment.getUserTransactionId())
                : assignHearingToSessionWithTransaction(hearing, targetSessions, assignment);
    }

    private UserTransaction assignHearingToSessionWithTransaction(Hearing hearing,
                                                                  List<Session> targetSessions,
                                                                  HearingSessionRelationship assignment)
                                                                        throws IOException {
        final String beforeHearing = objectMapper.writeValueAsString(hearing);
        entityManager.detach(hearing);
        hearing.setVersion(assignment.getHearingVersion());

        List<String> previousHearingPartsData = new ArrayList<>();
        List<String> factsMessages = new ArrayList<>();

        AtomicInteger index = new AtomicInteger();

        hearing.getHearingParts().stream().forEach(hp -> {
            try {
                previousHearingPartsData.add(objectMapper.writeValueAsString(hp));

                Session session = targetSessions.get(index.getAndIncrement());
                hp.setSessionId(session.getId());
                hp.setSession(session);
                if (targetSessions.size() > 1) {
                    hp.setStart(session.getStart());
                } else {
                    hp.setStart(assignment.getStart());
                }
                factsMessages.add(factsMapper.mapHearingToRuleJsonMessage(hp));
            } catch (JsonProcessingException e) {
                throw new SnlRuntimeException(e);
            }
        });

        UserTransaction ut = assignWithTransaction(hearing,
                assignment.getUserTransactionId(),
                targetSessions, beforeHearing, previousHearingPartsData);
        factsMessages.forEach(msg -> rulesService
            .postMessage(assignment.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg));

        return userTransactionService.rulesProcessed(ut);
    }

    private boolean areTransactionsInProgress(Hearing hearing, HearingSessionRelationship assignment) {
        List<UUID> idsList = hearing.getHearingParts().stream()
            .map(HearingPart::getId)
            .collect(Collectors.toList());

        idsList.add(hearing.getId());

        assignment.getSessionsData().forEach(session -> idsList.add(session.getSessionId()));

        return userTransactionService.isAnyBeingTransacted(idsList.stream().toArray(UUID[]::new));
    }

    private boolean areTransactionsInProgress(HearingPart hearingPart, HearingPartSessionRelationship assignment) {
        return userTransactionService.isAnyBeingTransacted(hearingPart.getId(),
            hearingPart.getSessionId(),
            hearingPart.getHearingId(),
            assignment.getSessionData().getSessionId());
    }

    public UserTransaction assignHearingPartToSessionWithTransaction(UUID hearingPartId,
                                                                     HearingPartSessionRelationship assignment)
        throws IOException {
        HearingPart hearingPart = hearingPartRepository.findOne(hearingPartId);
        Session targetSession = sessionRepository.findOne(assignment.getSessionData().getSessionId());

        return targetSession == null || areTransactionsInProgress(hearingPart, assignment)
            ? userTransactionService.transactionConflicted(assignment.getUserTransactionId())
            : assignHearingPartToSessionWithTransaction(hearingPart, targetSession, assignment);
    }

    private UserTransaction assignHearingPartToSessionWithTransaction(HearingPart hearingPart,
                                                                      Session targetSession,
                                                                      HearingPartSessionRelationship assignment)
        throws IOException {
        String previousHearingPart = objectMapper.writeValueAsString(hearingPart);
        String previousHearing = objectMapper.writeValueAsString(hearingPart.getHearing());
        hearingPart.setVersion(assignment.getHearingPartVersion());

        UUID targetSessionId = (targetSession == null) ? null : targetSession.getId();
        hearingPart.setSessionId(targetSessionId);
        hearingPart.setSession(targetSession);

        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        UserTransaction ut = assignWithTransaction(hearingPart,
            assignment.getUserTransactionId(),
            hearingPart.getSession(),
            targetSession, previousHearingPart, previousHearing);
        rulesService.postMessage(assignment.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

        return userTransactionService.rulesProcessed(ut);
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
