package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnlistHearingAction extends Action implements RulesProcessable {

    protected UnlistHearingRequest unlistHearingRequest;
    protected Hearing hearing;
    protected List<HearingPart> hearingParts;
    protected List<Session> sessions;

    protected HearingRepository hearingRepository;
    protected HearingPartRepository hearingPartRepository;

    public UnlistHearingAction(
        UnlistHearingRequest unlistHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository) {
            this.unlistHearingRequest = unlistHearingRequest;
            this.hearingRepository = hearingRepository;
            this.hearingPartRepository = hearingPartRepository;
    }

    @Override
    public void getAndValidateEntities() {
        hearing = hearingRepository.findOne(unlistHearingRequest.getHearingId());
        hearingParts = hearing.getHearingParts();
        sessions = hearingParts.stream()
            .map(hp -> hp.getSession())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (sessions.isEmpty()) {
            throw new RuntimeException("Hearing parts assigned to Hearing haven't been listed yet");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        List<UUID> ids = Arrays.asList(unlistHearingRequest.getHearingId());
        val hearingPartsIds = hearing.getHearingParts().stream().map(hp -> hp.getId()).collect(Collectors.toList());
        val sessionsIds = hearing.getHearingParts().stream()
            .map(hp -> hp.getSessionId())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        ids.addAll(hearingPartsIds);
        ids.addAll(sessionsIds);

        return ids.stream().toArray(UUID[]::new);
    }

    @Override
    public void act() {
        hearingParts.stream().forEach(hp -> {
            hp.setSession(null);
            hp.setStart(null);
        });

        hearingPartRepository.save(hearingParts);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        hearingParts.stream().forEach(hp -> {
            try {
                userTransactionDataList.add(new UserTransactionData("hearingPart",
                    hp.getId(),
                    objectMapper.writeValueAsString(hp),
                    "update",
                    "update",
                    0)
                );
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });

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

}
