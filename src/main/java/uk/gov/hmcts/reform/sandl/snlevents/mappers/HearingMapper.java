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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

@Component
public class HearingMapper {

    public List<HearingPart> mapToHearingParts(CreateHearingRequest createHearingRequest) {
// TODO: Think if this is needed, or BeanValidation is sufficient
//        if (createHearingRequest.getNumberOfSessions() == 0) {
//            throw new RuntimeException("New Hearing Should have NumberOfSession > 0");
//        }
        List<HearingPart> parts = new ArrayList<>();
        for (int i = 0; i < createHearingRequest.getNumberOfSessions(); i++) {
            HearingPart hearingPart = new HearingPart();
            hearingPart.setId(UUID.randomUUID());
            hearingPart.setHearingId(createHearingRequest.getId());
            parts.add(hearingPart);
        }
        return parts;
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
        hearing.setPriority(createHearingRequest.getPriority());

        if (createHearingRequest.getReservedJudgeId() != null) {
            hearing.setReservedJudge(
                entityManager.getReference(Person.class, createHearingRequest.getReservedJudgeId())
            );
        }

        return hearing;
    }
}
