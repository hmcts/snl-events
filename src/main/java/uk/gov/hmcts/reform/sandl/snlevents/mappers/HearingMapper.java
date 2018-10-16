package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;

import javax.persistence.EntityManager;
import java.util.UUID;

@Component
public class HearingMapper {
    public HearingPart mapToHearingPart(CreateHearingRequest createHearingRequest) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearingId(createHearingRequest.getId());

        return hearingPart;
    }

    public Hearing mapToHearing(CreateHearingRequest createHearingRequest,
                                CaseTypeRepository caseTypeRepository,
                                HearingTypeRepository hearingTypeRepository,
                                EntityManager entityManager) {
        Hearing hearing = new Hearing();
        hearing.setId(createHearingRequest.getId());
        hearing.setCaseNumber(createHearingRequest.getCaseNumber());
        hearing.setCaseTitle(createHearingRequest.getCaseTitle());
        CaseType caseType = caseTypeRepository.findOne(createHearingRequest.getCaseTypeCode());
        hearing.setCaseType(caseType);
        HearingType hearingType = hearingTypeRepository.findOne(createHearingRequest.getHearingTypeCode());
        hearing.setHearingType(hearingType);
        hearing.setDuration(createHearingRequest.getDuration());
        hearing.setScheduleStart(createHearingRequest.getScheduleStart());
        hearing.setScheduleEnd(createHearingRequest.getScheduleEnd());
        hearing.setCommunicationFacilitator(createHearingRequest.getCommunicationFacilitator());
        hearing.setReservedJudge(
            entityManager.getReference(Person.class, createHearingRequest.getReservedJudgeId())
        );
        hearing.setPriority(createHearingRequest.getPriority());

        return hearing;
    }
}
