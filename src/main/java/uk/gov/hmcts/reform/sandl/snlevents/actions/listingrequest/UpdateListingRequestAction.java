package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class UpdateListingRequestAction extends Action implements RulesProcessable {
    private List<HearingPart> hearingParts;
    private Hearing hearing;
    private List<Session> sessions;
    private UpdateListingRequest updateListingRequest;
    private String currentHearingAsString;
    private EntityManager entityManager;
    private HearingRepository hearingRepository;
    private HearingPartRepository hearingPartRepository;
    private StatusConfigService statusConfigService;
    private UserTransactionDataPreparerService utdps = new UserTransactionDataPreparerService();

    public UpdateListingRequestAction(UpdateListingRequest updateListingRequest,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper,
                                      HearingRepository hearingRepository,
                                      HearingPartRepository hearingPartRepository,
                                      StatusConfigService statusConfigService) {
        this.updateListingRequest = updateListingRequest;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.statusConfigService = statusConfigService;
    }

    @Override
    public void act() {
        try {
            currentHearingAsString = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        entityManager.detach(hearing);
        setUpdatedHearingValues();
        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(updateListingRequest.getId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        getHearingPartsWithStatus(hearing.getStatus().getStatus());
        getSessions();

        if (!hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() > 1) {
            throw new SnlEventsException("Single-session hearings cannot have more than 2 sessions!");
        }

        if (hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() < 2) {
            throw new SnlEventsException("Multi-session hearings cannot have less than 2 sessions!");
        }

        if (hearing.getStatus().getStatus().equals(Status.Listed)) {
            if (!OffsetDateTime.now().isBefore(hearingParts.get(0).getSession().getStart())) {
                throw new SnlEventsException("Cannot amend listing request if starts on or before today's date!");
            }

            if (updateListingRequest.getNumberOfSessions() > hearing.getNumberOfSessions()) {
                throw new SnlEventsException("Cannot increase number of sessions for a listed request!");
            }
        } else if (!hearing.getStatus().getStatus().equals(Status.Unlisted)) {
            throw new SnlEventsException("Cannot amend listing request that is neither listed or unlisted!");
        }
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return hearingParts.stream().map(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        hearingParts.forEach(hp ->
            utdps.prepareUserTransactionDataForUpdate("hearingPart", hp.getId(), null,  0)
        );

        utdps.prepareUserTransactionDataForUpdate("hearing", hearing.getId(), currentHearingAsString, 1);

        sessions.stream().forEach(s ->
            utdps.prepareLockedEntityTransactionData("session", s.getId(), 0)
        );

        return utdps.getUserTransactionDataList();
    }

    @Override
    public UUID getUserTransactionId() {
        return updateListingRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearingParts
            .stream()
            .map(HearingPart::getId)
            .collect(Collectors.toList());

        ids.addAll(hearingParts
            .stream()
            .filter(Objects::nonNull)
            .map(HearingPart::getSessionId)
            .collect(Collectors.toList()));

        ids.add(updateListingRequest.getId());

        return ids.stream().toArray(UUID[]::new);
    }

    private void getHearingPartsWithStatus(Status status) {
        hearingParts = hearing.getHearingParts()
                .stream()
                .filter(hp -> hp.getStatus().getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private void getSessions() {
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private void setUpdatedHearingValues() {
        hearing.setCaseNumber(updateListingRequest.getCaseNumber());
        hearing.setCaseTitle(updateListingRequest.getCaseTitle());
        hearing.setCaseType(
            this.entityManager.getReference(CaseType.class, updateListingRequest.getCaseTypeCode())
        );
        hearing.setHearingType(
            this.entityManager.getReference(HearingType.class, updateListingRequest.getHearingTypeCode())
        );
        hearing.setDuration(updateListingRequest.getDuration());
        hearing.setScheduleStart(updateListingRequest.getScheduleStart());
        hearing.setScheduleEnd(updateListingRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(updateListingRequest.getCommunicationFacilitator());
        hearing.setPriority(updateListingRequest.getPriority());
        hearing.setVersion(updateListingRequest.getVersion());
        setNumberOfSessionsAndHearingParts();
        setJudge();
    }

    private void setNumberOfSessionsAndHearingParts() {
        int diff = updateListingRequest.getNumberOfSessions() - hearing.getNumberOfSessions();
        if (diff > 0) {
            addHearingParts(diff);
        } else if (diff < 0) {
            removeHearingParts(Math.abs(diff));
        }

        hearing.setNumberOfSessions(updateListingRequest.getNumberOfSessions());
    }

    private void setJudge() {
        if (updateListingRequest.getReservedJudgeId() != null) {
            hearing.setReservedJudge(
                this.entityManager.getReference(Person.class, updateListingRequest.getReservedJudgeId())
            );
        } else {
            hearing.setReservedJudge(null);
        }
    }

    private void addHearingParts(int numberOfPartsToAdd) {
        for (int i = 0; i < numberOfPartsToAdd; i++) {
            HearingPart hearingPart = new HearingPart();
            hearingPart.setId(UUID.randomUUID());
            hearingPart.setHearingId(updateListingRequest.getId());
            hearingPart.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
            hearing.addHearingPart(hearingPart);
            hearingPartRepository.save(hearingPart);
        }
    }

    private void removeHearingParts(int numberOfPartsToRemove) {
        Status status = hearing.getStatus().getStatus().equals(Status.Listed) ? Status.Vacated : Status.Withdrawn;
        for (int i = hearingParts.size() - 1; i >= hearingParts.size() - numberOfPartsToRemove; i--) {
            HearingPart hp = hearingParts.get(i);
            hp.setSession(null);
            hp.setSessionId(null);
            hp.setStart(null);
            hp.setStatus(statusConfigService.getStatusConfig(status));
            hearingPartRepository.save(hp);
        }
    }
}
