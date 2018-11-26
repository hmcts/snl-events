package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.WithdrawHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class WithdrawHearingAction extends Action implements RulesProcessable {
    protected WithdrawHearingRequest withdrawHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;
    protected EntityManager entityManager;

    // id & hearing part string
    private Map<UUID, String> originalHearingParts;
    private String previousHearing;
    private UserTransactionDataPreparerService utdps = new UserTransactionDataPreparerService();

    public WithdrawHearingAction(
        WithdrawHearingRequest withdrawHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper,
        EntityManager entityManager
    ) {
        this.withdrawHearingRequest = withdrawHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(withdrawHearingRequest.getHearingId());
        hearingParts = hearing.getHearingParts();
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        // Validation moved to act() due to conflict with optimistic lock

        if (!statusServiceManager.canBeWithdrawn(hearing)) {
            throw new SnlEventsException("Hearing can not be withdrawn");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearing.getHearingParts().stream().map(HearingPart::getId).collect(Collectors.toList());
        ids.addAll(hearing.getHearingParts().stream()
            .map(HearingPart::getSessionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
        ids.add(withdrawHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }

        hearing.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));
        entityManager.detach(hearing);
        hearing.setVersion(withdrawHearingRequest.getHearingVersion());
        hearingRepository.save(hearing);

        originalHearingParts = utdps.mapHearingPartsToStrings(objectMapper, hearingParts);
        hearingParts.stream().forEach(hp -> {
            hp.setSession(null);
            hp.setSessionId(null);
            if (hp.getStatus().getStatus() == Status.Listed) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Vacated));
            } else if (hp.getStatus().getStatus() == Status.Unlisted) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));
            }
        });

        hearingPartRepository.save(hearingParts);
    }

    @Override
    @SuppressWarnings("DuplicatedBlocks")
    public List<UserTransactionData> generateUserTransactionData() {
        originalHearingParts.forEach((id, hpString) ->
            utdps.prepareUserTransactionDataForUpdate("hearingPart", id, hpString,  0)
        );

        utdps.prepareUserTransactionDataForUpdate("hearing", hearing.getId(),
            previousHearing, 1);

        sessions.stream().forEach(s ->
            utdps.prepareLockedEntityTransactionData("session", s.getId(), 0)
        );

        return utdps.getUserTransactionDataList();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            msgs.add(new FactMessage(RulesService.DELETE_HEARING_PART, msg));
        });

        return msgs;
    }

    @Override
    public UUID getUserTransactionId() {
        return withdrawHearingRequest.getUserTransactionId();
    }
}
