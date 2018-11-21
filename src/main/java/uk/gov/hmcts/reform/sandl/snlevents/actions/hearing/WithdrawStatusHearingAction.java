package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
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

public class WithdrawStatusHearingAction extends BaseStatusHearingAction implements RulesProcessable {

    public WithdrawStatusHearingAction(
        BaseStatusHearingRequest withdrawStatusHearingRequest,
        HearingRepository hearingRepository,
        HearingPartRepository hearingPartRepository,
        StatusConfigService statusConfigService,
        StatusServiceManager statusServiceManager,
        ObjectMapper objectMapper
    ) {
        super(withdrawStatusHearingRequest, hearingRepository, hearingPartRepository,
            statusConfigService, statusServiceManager, objectMapper);
    }

    @Override
    public void getAndValidateEntities() {
        super.getAndValidateEntities();

        if (!statusServiceManager.canBeWithdrawn(hearing)) {
            throw new SnlEventsException("Hearing can not be withdrawn");
        }
        hearingParts.forEach(hp -> {
            if (!statusServiceManager.canBeWithdrawn(hp)) {
                // we should define somewhere text of these messages and how much we want to show to the user
                throw new SnlEventsException("Hearing part can not be withdrawn");
            }
        });
    }

    @Override
    public void act() {
        hearing.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));

        originalHearingParts = mapHearingPartsToStrings(hearingParts);
        hearingParts.stream().forEach(hp -> {
            VersionInfo vi = getVersionInfo(hp);
            hp.setVersion(vi.getVersion());
            if (hp.getStatus().getStatus() == Status.Listed) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Vacated));
            } else if (hp.getStatus().getStatus() == Status.Unlisted) {
                hp.setStatus(statusConfigService.getStatusConfig(Status.Withdrawn));
            }
        });

        hearingPartRepository.save(hearingParts);
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
}
