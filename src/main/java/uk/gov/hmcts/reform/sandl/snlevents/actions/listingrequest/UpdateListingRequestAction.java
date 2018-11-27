package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.*;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class UpdateListingRequestAction extends Action implements RulesProcessable {

    private List<HearingPart> hearingParts;
    private Hearing hearing;
    private UpdateListingRequest updateListingRequest;
    private String currentHearingAsString;
    private EntityManager entityManager;
    private HearingTypeRepository hearingTypeRepository;
    private CaseTypeRepository caseTypeRepository;
    private HearingRepository hearingRepository;
    private HearingPartRepository hearingPartRepository;
    private StatusConfigService statusConfigService;

    public UpdateListingRequestAction(UpdateListingRequest updateListingRequest,
                                      EntityManager entityManager,
                                      ObjectMapper objectMapper,
                                      HearingTypeRepository hearingTypeRepository,
                                      CaseTypeRepository caseTypeRepository,
                                      HearingRepository hearingRepository,
                                      HearingPartRepository hearingPartRepository,
                                      StatusConfigService statusConfigService) {
        this.updateListingRequest = updateListingRequest;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.hearingTypeRepository = hearingTypeRepository;
        this.caseTypeRepository = caseTypeRepository;
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

        hearing.setCaseNumber(updateListingRequest.getCaseNumber());
        hearing.setCaseTitle(updateListingRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(updateListingRequest.getCaseTypeCode());
        hearing.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(updateListingRequest.getHearingTypeCode());
        hearing.setHearingType(hearingType);
        hearing.setDuration(updateListingRequest.getDuration());
        hearing.setScheduleStart(updateListingRequest.getScheduleStart());
        hearing.setScheduleEnd(updateListingRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(updateListingRequest.getCommunicationFacilitator());
        hearing.setPriority(updateListingRequest.getPriority());
        hearing.setVersion(updateListingRequest.getVersion());

        int diff = updateListingRequest.getNumberOfSessions() - hearing.getNumberOfSessions();
        if (diff > 0) {
            addHearingParts(diff);
        } else if (diff < 0) {
            removeHearingParts(diff * -1);
        }

        hearing.setNumberOfSessions(updateListingRequest.getNumberOfSessions());

        if (updateListingRequest.getReservedJudgeId() != null) {
            hearing.setReservedJudge(
                this.entityManager.getReference(Person.class, updateListingRequest.getReservedJudgeId())
            );
        } else {
            hearing.setReservedJudge(null);
        }

        hearingRepository.save(hearing);
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(updateListingRequest.getId());
        hearingParts = hearing.getHearingParts();
        hearingParts.sort(Comparator.comparing(a -> a.getSession().getStart()));

        if (hearing == null) {
            throw new EntityNotFoundException("Hearing not found");
        }

        if (hearing.getStatus().getStatus().equals(Status.Listed)) {
            if (!OffsetDateTime.now().toLocalDate().isBefore(
                hearingParts.get(0).getSession().getStart().toLocalDate())) {
                throw new SnlEventsException("Cannot amend listing request if starts on or before today's date!");
            }

            if (updateListingRequest.getNumberOfSessions() > hearing.getNumberOfSessions()) {
                throw new SnlEventsException("Cannot increase number of sessions for a listed request!");
            }
        } else if (!hearing.getStatus().getStatus().equals(Status.Unlisted)) {
            throw new SnlEventsException("Cannot amend listing request that is neither listed or unlisted!");
        }

        if (!hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() > 1) {
            throw new SnlEventsException("Single-session hearings cannot have more than 2 sessions!");
        }

        if (hearing.isMultiSession() && updateListingRequest.getNumberOfSessions() < 2) {
            throw new SnlEventsException("Multi-session hearings cannot have less than 2 sessions!");
        }
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        return hearing.getHearingParts().stream().map(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            return new FactMessage(RulesService.UPSERT_HEARING_PART, msg);
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
            currentHearingAsString,
            "update",
            "update",
            0));

        hearing.getHearingParts().forEach(hp -> userTransactionDataList.add(new UserTransactionData("hearingPart",
            hp.getId(),
            null,
            "lock",
            "unlock",
            0)));

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return updateListingRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[]{updateListingRequest.getId()};
    }

    private void addHearingParts(int numberOfPartsToAdd) {
        for (int i = 0; i < numberOfPartsToAdd; i++) {
            HearingPart hearingPart = new HearingPart();
            hearingPart.setId(UUID.randomUUID());
            hearingPart.setHearingId(updateListingRequest.getId());
            hearingPart.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
            hearingPart.setHearing(hearing);
            hearing.addHearingPart(hearingPart);
        }
    }

    private void removeHearingParts(int numberOfPartsToRemove) {
        Status status = hearing.getStatus().getStatus().equals(Status.Listed) ? Status.Vacated : Status.Withdrawn;
        for (int i = hearingParts.size() - 1; i >= hearingParts.size() - numberOfPartsToRemove; i--) {
            HearingPart hp = hearingParts.get(i);
            hp.setSession(null);
            hp.setSessionId(null);
            hp.setStatus(statusConfigService.getStatusConfig(status));
            hearingPartRepository.save(hp);
        }
    }
}
