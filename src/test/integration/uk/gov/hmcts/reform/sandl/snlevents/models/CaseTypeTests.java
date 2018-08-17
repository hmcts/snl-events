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
public class CaseTypeTests extends BaseIntegrationModelTest {
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;

    CaseType caseType = new CaseType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSessionType_shouldSetCorrespondentRelationInSessionType2() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        caseType.addSessionType(sessionType);
        new ReferenceDataValidator<CaseType, SessionType, String, String>()
            .save(caseTypeRepository, caseType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, sessionTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(CaseType::getSessionTypes, SessionType::getCaseTypes);
    }

    @Test
    public void addHearingType_shouldSetCorrespondentRelationInHearingType2() {
        HearingType hearingType = new HearingType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        this.caseType.addHearingType(hearingType);
        new ReferenceDataValidator<CaseType, HearingType, String, String>()
            .save(caseTypeRepository, caseType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, hearingTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(CaseType::getHearingTypes, HearingType::getCaseTypes);
    }
}

