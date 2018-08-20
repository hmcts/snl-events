package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.ReferenceDataValidator;

import javax.transaction.Transactional;

@Transactional
public class HearingTypeTests extends BaseIntegrationModelTest  {
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;

    HearingType hearingType = new HearingType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSessionType_shouldSetCorrespondentRelationInSessionType() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        hearingType.addSessionType(sessionType);

        new ReferenceDataValidator<HearingType, SessionType, String, String>()
            .save(hearingTypeRepository, hearingType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, sessionTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(HearingType::getSessionTypes, SessionType::getHearingTypes);
    }

    @Test
    public void addCaseType_shouldSetCorrespondentRelationInCaseType() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        hearingType.addCaseType(caseType);

        new ReferenceDataValidator<HearingType, CaseType, String, String>()
            .save(hearingTypeRepository, hearingType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, caseTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(HearingType::getCaseTypes, CaseType::getHearingTypes);
    }
}
