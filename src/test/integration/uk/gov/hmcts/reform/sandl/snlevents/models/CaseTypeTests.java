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

        caseTypeRepository.saveAndFlush(caseType);

        SessionType savedRoomType = sessionTypeRepository.findById(REF_TYPE_CODE).orElse(null);
        CaseType savedCaseType = caseTypeRepository.findById(MAIN_TYPE_CODE).orElse(null);

        assertThat(savedRoomType.getCaseTypes().size()).isEqualTo(1);
        assertThat(savedCaseType.getSessionTypes().contains(sessionType)).isTrue();

    }

    @Test
    public void addHearingType_shouldSetCorrespondentRelationInHearingType2() {
        HearingType hearingType = new HearingType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        this.caseType.addHearingType(hearingType);

        caseTypeRepository.saveAndFlush(caseType);

        HearingType savedHearingType = hearingTypeRepository.findById(REF_TYPE_CODE).orElse(null);
        CaseType savedCaseType = caseTypeRepository.findById(MAIN_TYPE_CODE).orElse(null);

        assertThat(savedHearingType.getCaseTypes().size()).isEqualTo(1);
        assertThat(savedCaseType.getHearingTypes().contains(savedHearingType)).isTrue();
    }
}

