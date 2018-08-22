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
public class CaseTypeTests extends ReferenceData {
    CaseType caseType = new CaseType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSessionType_addSessionTypeToSessionTypes() {
        SessionType sessionType = new SessionType("code", "description");

        caseType.addSessionType(sessionType);

        assertThat(caseType.getSessionTypes().size()).isEqualTo(1);
        assertThat(caseType.getSessionTypes().contains(sessionType)).isTrue();
    }

    @Test
    public void addHearingType_addHearingTypeToHearingTypes() {
        HearingType hearingType = new HearingType("code", "description");

        caseType.addHearingType(hearingType);

        assertThat(caseType.getHearingTypes().size()).isEqualTo(1);
        assertThat(caseType.getHearingTypes().contains(hearingType)).isTrue();
    }
}
