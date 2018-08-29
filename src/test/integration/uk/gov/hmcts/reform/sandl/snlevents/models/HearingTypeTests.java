package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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

        hearingTypeRepository.saveAndFlush(hearingType);

        SessionType savedSessionType = sessionTypeRepository.findOne(REF_TYPE_CODE);
        HearingType savedHearingType = hearingTypeRepository.findOne(MAIN_TYPE_CODE);

        assertThat(savedSessionType.getHearingTypes().size()).isEqualTo(1);
        assertThat(savedHearingType.getSessionTypes().contains(sessionType)).isTrue();
    }

    @Test
    public void addCaseType_shouldSetCorrespondentRelationInCaseType() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        hearingType.addCaseType(caseType);

        hearingTypeRepository.saveAndFlush(hearingType);

        CaseType savedCaseType = caseTypeRepository.findOne(REF_TYPE_CODE);
        HearingType savedHearingType = hearingTypeRepository.findOne(MAIN_TYPE_CODE);

        assertThat(savedCaseType.getHearingTypes().size()).isEqualTo(1);
        assertThat(savedHearingType.getCaseTypes().contains(caseType)).isTrue();
    }
}
