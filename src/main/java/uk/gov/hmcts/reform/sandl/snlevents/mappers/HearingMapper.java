package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;

import java.util.UUID;

@Component
public class HearingMapper {
    public HearingPart mapToHearingPart(CreateHearingPartRequest createHearingPartRequest) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearingId(createHearingPartRequest.getId());

        return hearingPart;
    }

    public Hearing mapToHearing(CreateHearingPartRequest createHearingPartRequest,
                                CaseTypeRepository caseTypeRepository,
                                HearingTypeRepository hearingTypeRepository) {
        Hearing hearing = new Hearing();
        hearing.setId(createHearingPartRequest.getId());
        hearing.setCaseNumber(createHearingPartRequest.getCaseNumber());
        hearing.setCaseTitle(createHearingPartRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(createHearingPartRequest.getCaseTypeCode());
        hearing.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(createHearingPartRequest.getHearingTypeCode());
        hearing.setHearingType(hearingType);
        hearing.setDuration(createHearingPartRequest.getDuration());
        hearing.setScheduleStart(createHearingPartRequest.getScheduleStart());
        hearing.setScheduleEnd(createHearingPartRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(createHearingPartRequest.getCommunicationFacilitator());
        hearing.setReservedJudgeId(createHearingPartRequest.getReservedJudgeId());
        hearing.setPriority(createHearingPartRequest.getPriority());

        return hearing;
    }
}
