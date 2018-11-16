package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnlistHearingAction extends Action implements RulesProcessable {

    protected UnlistHearingRequest unlistHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;

    // id & hearing part string
    private Map<UUID, String> originalHearingParts;

    public UnlistHearingAction(
        UnlistHearingRequest unlistHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper
    ) {
        this.unlistHearingRequest = unlistHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(unlistHearingRequest.getHearingId());
        hearingParts = hearing.getHearingParts();
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        // Validation moved to act() due to conflict with optimistic lock

        if (!statusServiceManager.canBeUnlisted(hearing)) {
            throw new SnlEventsException("Hearing can not be unlisted");
        }
        hearingParts.forEach(hp -> {
            if (!statusServiceManager.canBeUnlisted(hp)) {
                // we should define somewhere text of these messages and how much we want to show to the user
                throw new SnlEventsException("Hearing part can not be unlisted");
            }
        });
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearing.getHearingParts().stream().map(HearingPart::getId).collect(Collectors.toList());
        ids.addAll(hearing.getHearingParts().stream()
            .map(HearingPart::getSessionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
        ids.add(unlistHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        if (sessions.isEmpty()) {
            throw new EntityNotFoundException("Hearing parts assigned to Hearing haven't been listed yet");
        }

        hearing.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));

        originalHearingParts = mapHearingPartsToStrings(hearingParts);
        hearingParts.stream().forEach(hp -> {
            hp.setSession(null);
            hp.setSessionId(null);
            hp.setStart(null);
            VersionInfo vi = getVersionInfo(hp);
            hp.setVersion(vi.getVersion());
            hp.setStatus(statusConfigService.getStatusConfig(Status.Unlisted));
        });

        hearingPartRepository.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        originalHearingParts.forEach((id, hpString) ->
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                id,
                hpString,
                "update",
                "update",
                0)
            )
        );

        userTransactionDataList.add(prepareLockedEntityTransactionData("hearing", hearing.getId()));
        sessions.stream().forEach(s ->
            userTransactionDataList.add(prepareLockedEntityTransactionData("session", s.getId()))
        );

        return userTransactionDataList;
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            msgs.add(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
        });

        return msgs;
    }

    @Override
    public UUID getUserTransactionId() {
        return unlistHearingRequest.getUserTransactionId();
    }

    private VersionInfo getVersionInfo(HearingPart hp) {
        Optional<VersionInfo> hpvi = unlistHearingRequest.getHearingPartsVersions()
            .stream()
            .filter(hpv -> hpv.getId().equals(hp.getId()))
            .findFirst();
        return hpvi.orElseThrow(() ->
            new EntityNotFoundException("Couldn't find version for hearing part with id " + hp.getId().toString())
        );
    }

    private UserTransactionData prepareLockedEntityTransactionData(String entity, UUID id) {
        return new UserTransactionData(entity, id, null, "lock", "unlock", 0);
    }

    private Map<UUID, String> mapHearingPartsToStrings(List<HearingPart> hearingParts) {
        Map<UUID, String> originalIdStringPair = new HashMap<>();
        hearingParts.stream().forEach(hp -> {
            try {
                String hearingPartString = objectMapper.writeValueAsString(hp);
                originalIdStringPair.put(hp.getId(), hearingPartString);
            } catch (JsonProcessingException e) {
                throw new SnlRuntimeException(e);
            }
        });

        return originalIdStringPair;
    }
}
