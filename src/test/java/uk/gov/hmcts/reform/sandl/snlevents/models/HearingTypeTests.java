package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.ReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingTypeTests extends ReferenceData {
    HearingType hearingType = new HearingType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSessionType_addSessionTypeToSessionTypes() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);

        hearingType.addSessionType(sessionType);

        assertThat(hearingType.getSessionTypes().size()).isEqualTo(1);
        assertThat(hearingType.getSessionTypes().contains(sessionType)).isTrue();
    }

    @Test
    public void addCaseType_addCaseTypeToCaseTypes() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);

        hearingType.addCaseType(caseType);

        assertThat(hearingType.getCaseTypes().size()).isEqualTo(1);
        assertThat(hearingType.getCaseTypes().contains(caseType)).isTrue();
    }
}
