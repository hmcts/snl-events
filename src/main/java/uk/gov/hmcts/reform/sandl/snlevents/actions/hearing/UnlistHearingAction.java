package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnlistHearingAction extends Action implements RulesProcessable {

    private UnlistHearingRequest unlistHearingRequest;
    private Hearing hearing;
    private List<HearingPart> hearingParts;
    private List<Session> sessions;

    private HearingRepository hearingRepository;
    private HearingPartRepository hearingPartRepository;

    // id & hearing part string
    private Map<UUID, String> originalHearingParts;

    public UnlistHearingAction(
        UnlistHearingRequest unlistHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        ObjectMapper objectMapper) {
        this.unlistHearingRequest = unlistHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
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
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        List<UUID> ids = new LinkedList<>(Arrays.asList(unlistHearingRequest.getHearingId()));
        val hearingPartsIds = hearing.getHearingParts().stream().map(HearingPart::getId).collect(Collectors.toList());
        val sessionsIds = hearing.getHearingParts().stream()
            .map(HearingPart::getSessionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        ids.addAll(hearingPartsIds);
        ids.addAll(sessionsIds);

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        if (sessions.isEmpty()) {
            throw new RuntimeException("Hearing parts assigned to Hearing haven't been listed yet");
        }

        originalHearingParts = mapHearingPartsToStrings(hearingParts);

        hearingParts.stream().forEach(hp -> {
            hp.setSession(null);
            hp.setSessionId(null);
            hp.setStart(null);
            VersionInfo vi = getVersionInfo(hp);
            hp.setVersion(vi.getVersion());
        });

        hearingPartRepository.save(hearingParts);
    }

    private VersionInfo getVersionInfo(HearingPart hp) {
        Optional<VersionInfo> hpvi = unlistHearingRequest.getHearingPartsVersions()
            .stream()
            .filter(hpv -> hpv.getId().equals(hp.getId()))
            .findFirst();
        return hpvi.orElseThrow(() ->
            new RuntimeException("Couldn't find version for hearing part with id " + hp.getId().toString())
        );
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

        userTransactionDataList.add(getLockedEntityTransactionData("hearing", hearing.getId()));
        sessions.stream().forEach(s ->
            userTransactionDataList.add(getLockedEntityTransactionData("session", s.getId()))
        );

        return userTransactionDataList;
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg;
            try {
                msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            msgs.add(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
        });

        return msgs;
    }

    @Override
    public UUID getUserTransactionId() {
        return unlistHearingRequest.getUserTransactionId();
    }

    private UserTransactionData getLockedEntityTransactionData(String entity, UUID id) {
        return new UserTransactionData(entity, id, null, "lock", "unlock", 0);
    }

    private Map<UUID, String> mapHearingPartsToStrings(List<HearingPart> hearingParts) {
        Map<UUID, String> originalIdStringPair = new HashMap<UUID, String>();
        hearingParts.stream().forEach(hp -> {
            try {
                String hearingPartString = objectMapper.writeValueAsString(hp);
                originalIdStringPair.put(hp.getId(), hearingPartString);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return originalIdStringPair;
    }
}
