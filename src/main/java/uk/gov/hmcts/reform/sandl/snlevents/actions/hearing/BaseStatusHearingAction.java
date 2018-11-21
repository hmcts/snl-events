package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.BaseStatusHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
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

public class BaseStatusHearingAction extends Action {
    protected BaseStatusHearingRequest baseStatusHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;
    protected StatusConfigService statusConfigService;
    protected StatusServiceManager statusServiceManager;

    // id & hearing part string
    protected Map<UUID, String> originalHearingParts;
    protected String previousHearing;

    public BaseStatusHearingAction(
        BaseStatusHearingRequest baseStatusHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper
    ) {
        this.baseStatusHearingRequest = baseStatusHearingRequest;
        this.hearingRepository = hearingRepository;
        this.hearingPartRepository = hearingPartRepository;
        this.statusConfigService = statusConfigService;
        this.statusServiceManager = statusServiceManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(baseStatusHearingRequest.getHearingId());
        hearingParts = hearing.getHearingParts();
        sessions = hearingParts.stream()
            .map(HearingPart::getSession)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        val ids = hearing.getHearingParts().stream().map(HearingPart::getId).collect(Collectors.toList());
        ids.addAll(hearing.getHearingParts().stream()
            .map(HearingPart::getSessionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
        ids.add(baseStatusHearingRequest.getHearingId());

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        if (sessions.isEmpty()) {
            throw new EntityNotFoundException("Hearing parts assigned to Hearing haven't been listed yet");
        }
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }
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

        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
            previousHearing,
            "update",
            "update",
            1)
        );

        sessions.stream().forEach(s ->
            userTransactionDataList.add(prepareLockedEntityTransactionData("session", s.getId()))
        );

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return baseStatusHearingRequest.getUserTransactionId();
    }

    protected VersionInfo getVersionInfo(HearingPart hp) {
        Optional<VersionInfo> hpvi = baseStatusHearingRequest.getHearingPartsVersions()
            .stream()
            .filter(hpv -> hpv.getId().equals(hp.getId()))
            .findFirst();
        return hpvi.orElseThrow(() ->
            new EntityNotFoundException("Couldn't find version for hearing part with id " + hp.getId().toString())
        );
    }

    protected Map<UUID, String> mapHearingPartsToStrings(List<HearingPart> hearingParts) {
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

    private UserTransactionData prepareLockedEntityTransactionData(String entity, UUID id) {
        return new UserTransactionData(entity, id, null, "lock", "unlock", 0);
    }
}
