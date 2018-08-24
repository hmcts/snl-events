package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.ReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class SessionTypeTests extends ReferenceData {
    SessionType sessionType = new SessionType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSession_addSessionToSessions() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setDuration(Duration.ofHours(1));
        session.setStart(OffsetDateTime.now());

        sessionType.addSession(session);

        assertThat(sessionType.getSessions().size()).isEqualTo(1);
        assertThat(sessionType.getSessions().contains(session)).isTrue();
    }

    @Test
    public void addHearingType_addHearingTypeToHearingTypes() {
        HearingType hearingType = new HearingType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);

        sessionType.addHearingType(hearingType);

        assertThat(sessionType.getHearingTypes().size()).isEqualTo(1);
        assertThat(sessionType.getHearingTypes().contains(hearingType)).isTrue();
    }

    @Test
    public void addCaseType_addCaseTypeToCaseTypes() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);

        sessionType.addCaseType(caseType);

        assertThat(sessionType.getCaseTypes().size()).isEqualTo(1);
        assertThat(sessionType.getCaseTypes().contains(caseType)).isTrue();
    }
}
