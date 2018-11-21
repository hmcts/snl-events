package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.BaseStatusHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;

public class UnlistStatusHearingAction extends BaseStatusHearingAction implements RulesProcessable {

    public UnlistStatusHearingAction(
        BaseStatusHearingRequest baseStatusHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper
    ) {
        super(baseStatusHearingRequest, hearingRepository, hearingPartRepository,
            statusConfigService, statusServiceManager, objectMapper);
    }

    @Override
    public void getAndValidateEntities() {
        super.getAndValidateEntities();

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
    public void act() {
        // Validation moved to act() due to conflict with optimistic lock
        if (sessions.isEmpty()) {
            throw new EntityNotFoundException("Hearing parts assigned to Hearing haven't been listed yet");
        }
        try {
            previousHearing = objectMapper.writeValueAsString(hearing);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
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
    public List<FactMessage> generateFactMessages() {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            msgs.add(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
        });

        return msgs;
    }
}
