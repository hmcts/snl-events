package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;

@Component
public class HearingPartMapper {
    public HearingPart mapToHearingPart(CreateHearingPartRequest createHearingPartRequest,
                                        CaseTypeRepository caseTypeRepository,
                                        HearingTypeRepository hearingTypeRepository) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createHearingPartRequest.getId());
        hearingPart.setCaseNumber(createHearingPartRequest.getCaseNumber());
        hearingPart.setCaseTitle(createHearingPartRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(createHearingPartRequest.getCaseTypeCode());
        hearingPart.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(createHearingPartRequest.getHearingTypeCode());
        hearingPart.setHearingType(hearingType);
        hearingPart.setDuration(createHearingPartRequest.getDuration());
        hearingPart.setScheduleStart(createHearingPartRequest.getScheduleStart());
        hearingPart.setScheduleEnd(createHearingPartRequest.getScheduleEnd());
        hearingPart.setCommunicationFacilitator(createHearingPartRequest.getCommunicationFacilitator());
        hearingPart.setReservedJudgeId(createHearingPartRequest.getReservedJudgeId());
        hearingPart.setPriority(createHearingPartRequest.getPriority());

        return hearingPart;
    }
}
