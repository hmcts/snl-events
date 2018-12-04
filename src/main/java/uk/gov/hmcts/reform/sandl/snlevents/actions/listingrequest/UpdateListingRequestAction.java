package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
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
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class UpdateListingRequestAction extends Action implements RulesProcessable {
    private List<HearingPart> hearingParts;
    private List<HearingPart> hearingPartsToAdd = new ArrayList<>();
    private List<HearingPart> hearingPartsToRemove = new ArrayList<>();
    private Hearing hearing;
    private List<Session> sessions;
    private UpdateListingRequest updateListingRequest;
    private String currentHearingAsString;
    private EntityManager entityManager;
    private HearingRepository hearingRepository;
    private StatusConfigService statusConfigService;
    private UserTransactionDataPreparerService utdps = new UserTransactionDataPreparerService();

    public UpdateListingRequestAction(UpdateListingRequest updateListingRequest,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper,
                                      HearingRepository hearingRepository,
                                      StatusConfigService statusConfigService) {
        this.updateListingRequest = updateListingRequest;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
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
        updateHearing();
        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(updateListingRequest.getId());

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        if (!hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() > 1) {
            throw new SnlEventsException("Single-session hearings cannot have more than 2 sessions!");
        }

        if (hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() < 2) {
            throw new SnlEventsException("Multi-session hearings cannot have less than 2 sessions!");
        }

        if (hearing.getStatus().getStatus().equals(Status.Listed)) {
            getListedHearingParts();
            if (!OffsetDateTime.now().isBefore(hearingParts.get(0).getSession().getStart())) {
                throw new SnlEventsException("Cannot amend listing request if starts on or before today's date!");
            }

            if (updateListingRequest.getNumberOfSessions() > hearing.getNumberOfSessions()) {
                throw new SnlEventsException("Cannot increase number of sessions for a listed request!");
            }
        } else if (hearing.getStatus().getStatus().equals(Status.Unlisted)) {
            getUnlistedHearingParts();
        } else {
            throw new SnlEventsException("Cannot amend listing request that is neither listed or unlisted!");
        }

        getHearingParts();
        getSessions();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return utdps.generateUpsertHearingPartFactMsg(hearingParts, factsMapper);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        hearingPartsToAdd.forEach(hp ->
            utdps.prepareUserTransactionDataForCreate("hearingPart", hp.getId(),  0)
        );

        hearingPartsToRemove.forEach(hp ->
            utdps.prepareUserTransactionDataForUpdate("hearingPart", hp.getId(), getHearingPartString(hp),  0)
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
        val ids = hearingPartsToAdd
            .stream()
            .map(HearingPart::getId)
            .collect(Collectors.toList());

        ids.addAll(hearingPartsToRemove
            .stream()
            .map(HearingPart::getId)
            .collect(Collectors.toList())
        );

        if (hearing.getStatus().getStatus().equals(Status.Listed)) {
            ids.addAll(hearingPartsToRemove
                .stream()
                .filter(Objects::nonNull)
                .map(HearingPart::getSessionId)
                .collect(Collectors.toList()));
        }

        ids.add(updateListingRequest.getId());

        return ids.stream().toArray(UUID[]::new);
    }

    private void getUnlistedHearingParts() {
        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> hp.getStatus().getStatus().equals(Status.Unlisted))
            .collect(Collectors.toList());
    }

    private void getListedHearingParts() {
        hearingParts = hearing.getHearingParts()
            .stream()
            .filter(hp -> hp.getStatus().getStatus().equals(Status.Listed))
            .sorted(Comparator.comparing(HearingPart::getStart))
            .collect(Collectors.toList());
    }

    private void getHearingParts() {
        int diff = updateListingRequest.getNumberOfSessions() - hearing.getNumberOfSessions();
        if (diff > 0) {
            addHearingParts(diff);
        } else if (diff < 0) {
            removeHearingParts(Math.abs(diff));
        }
    }

    private void addHearingParts(int numberOfPartsToAdd) {
        for (int i = 0; i < numberOfPartsToAdd; i++) {
            HearingPart hp = new HearingPart();
            hp.setId(UUID.randomUUID());
            hp.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
            hearingPartsToAdd.add(hp);
            hearing.addHearingPart(hp);
        }
    }

    private void removeHearingParts(int numberOfPartsToRemove) {
        hearingPartsToRemove = hearingParts.subList(hearingParts.size() - numberOfPartsToRemove,
            hearingParts.size());
        Status status = hearing.getStatus().getStatus().equals(Status.Listed) ? Status.Vacated : Status.Withdrawn;
        for (HearingPart hp : hearingPartsToRemove) {
            hp.setSession(null);
            hp.setSessionId(null);
            hp.setStart(null);
            hp.setStatus(statusConfigService.getStatusConfig(status));
        }
    }

    private String getHearingPartString(HearingPart hp) {
        String hearingPartString;
        try {
            hearingPartString = objectMapper.writeValueAsString(hp);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }

        return hearingPartString;
    }

    private void getSessions() {
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private void updateHearing() {
        hearing.setCaseNumber(updateListingRequest.getCaseNumber());
        hearing.setCaseTitle(updateListingRequest.getCaseTitle());
        hearing.setDuration(updateListingRequest.getDuration());
        hearing.setScheduleStart(updateListingRequest.getScheduleStart());
        hearing.setScheduleEnd(updateListingRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(updateListingRequest.getCommunicationFacilitator());
        hearing.setPriority(updateListingRequest.getPriority());
        hearing.setVersion(updateListingRequest.getVersion());
        hearing.setNumberOfSessions(updateListingRequest.getNumberOfSessions());
        hearing.setCaseType(
            this.entityManager.getReference(CaseType.class, updateListingRequest.getCaseTypeCode())
        );
        hearing.setHearingType(
            this.entityManager.getReference(HearingType.class, updateListingRequest.getHearingTypeCode())
        );

        if (updateListingRequest.getReservedJudgeId() != null) {
            hearing.setReservedJudge(
                this.entityManager.getReference(Person.class, updateListingRequest.getReservedJudgeId())
            );
        } else {
            hearing.setReservedJudge(null);
        }
    }
}
