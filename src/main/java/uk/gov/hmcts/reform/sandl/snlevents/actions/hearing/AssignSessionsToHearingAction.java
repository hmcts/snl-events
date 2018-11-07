//package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;
//
//import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
//import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
//import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
//import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
//import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
//import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
//import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
//import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
//import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
//import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
//import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//public class AssignSessionsToHearingAction extends Action implements RulesProcessable {
//
//    protected HearingSessionRelationship hearingSessionRelationship;
//    protected UUID hearingPartId;
//    protected HearingPart hearingPart;
//    protected List<Session> targetSessions;
//    protected List<UUID> targetSessionsIds;
//
//    protected HearingPartRepository hearingPartRepository;
//
//    protected SessionRepository sessionRepository;
//    private String previousHearingPart;
//    private String previousHearing;
//
//    //Done
//    public AssignSessionsToHearingAction(UUID hearingPartId,
//                                         HearingSessionRelationship hearingSessionRelationship,
//                                         HearingPartRepository hearingPartRepository,
//                                         SessionRepository sessionRepository) {
//        this.hearingSessionRelationship = hearingSessionRelationship;
//        this.hearingPartId = hearingPartId;
//        this.hearingPartRepository = hearingPartRepository;
//        this.sessionRepository = sessionRepository;
//    }
//
//    @Override //Done
//    public void getAndValidateEntities() {
//        hearingPart = hearingPartRepository.findOne(hearingPartId);
//
//        targetSessionsIds = hearingSessionRelationship.getSessionsData().stream()
//            .map(SessionAssignmentData::getSessionId)
//            .collect(Collectors.toList());
//        targetSessions = sessionRepository.findSessionByIdIn(targetSessionsIds);
//
//        if (targetSessions == null) {
//            throw new RuntimeException("Target sessions cannot be null!");
//        } else if (hearingPart == null) {
//            throw new RuntimeException("Hearing part cannot be null!");
//        }
//    }
//
//    //Done
//    @Override
//    public UUID[] getAssociatedEntitiesIds() {
//        final List<UUID> entitiesIds = Arrays.asList(
//            hearingPart.getId(), hearingPart.getSessionId(), hearingPart.getHearingId()
//        );
//        entitiesIds.addAll(targetSessionsIds);
//
//        return entitiesIds.toArray(new UUID[0]);
//    }
//
//    @Override
//    public void act() {
////        hearingPart.setSession(targetSession);
////        hearingPart.setSessionId(targetSession.getId());
////        //hearingPart.setStart(hearingSessionRelationship.getStart());
//
//        previousHearingPart = objectMapper.writeValueAsString(hearingPart);
//        previousHearing = objectMapper.writeValueAsString(hearingPart.getHearing());
//// Brakuje wersji w requescie        hearingPart.setVersion(assignment.getHearingPartVersion());
//
//        UUID targetSessionId = (targetSession == null) ? null : targetSession.getId();
//        hearingPart.setSessionId(targetSessionId);
//        hearingPart.setSession(targetSession);
//
//        hearingPartRepository.save(hearingPart);
//    }
//
//    @Override
//    public List<UserTransactionData> generateUserTransactionData() {
//
//        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
//        userTransactionDataList.add(new UserTransactionData("hearing",
//            savedHearing.getId(),
//            beforeHearing,
//            "update",
//            "update",
//            0)
//        );
//
//        AtomicInteger index = new AtomicInteger();
//
//        userTransactionDataList.add(new UserTransactionData("hearingPart",
//            hearingPart.getId(),
//            previousHearingParts.get(index.getAndIncrement()),
//            "update",
//            "update",
//            1
//        ));
//
//        targetSessions.forEach(session ->
//            userTransactionDataList.add(getLockedSessionTransactionData(session.getId()))
//        );
//        //////
//        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
//        try {
//            userTransactionDataList.add(new UserTransactionData("hearingPart",
//                hearingPart.getId(),
//                objectMapper.writeValueAsString(hearingPart),
//                "update",
//                "update",
//                0)
//            );
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//
//        if (hearingPart.getSession() != null) {
//            userTransactionDataList.add(getLockedSessionTransactionData(hearingPart.getSession().getId()));
//        }
//        userTransactionDataList.add(getLockedSessionTransactionData(targetSession.getId()));
//
//        return userTransactionDataList;
//    }
//
//    @Override
//    public FactMessage generateFactMessage() {
//        String msg = null;
//        //try {
//        //    msg = factsMapper.mapHearingToRuleJsonMessage(hearingPart); @TODO use Action instead of service
//        //} catch (JsonProcessingException e) {
//        //    throw new RuntimeException(e);
//        //}
//
//        return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
//    }
//
//    //Done
//    @Override
//    public UUID getUserTransactionId() {
//        return hearingSessionRelationship.getUserTransactionId();
//    }
//
//    private UserTransactionData getLockedSessionTransactionData(UUID id) {
//        return new UserTransactionData("session", id, null, "lock", "unlock", 0);
//    }
//}
