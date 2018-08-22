package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.ReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class SessionTests extends ReferenceData {
    Session session = new Session();

    @Before
    public void setUp() {
        session.setId(UUID.randomUUID());
        session.setDuration(Duration.ofHours(1));
        session.setStart(OffsetDateTime.now());
    }

    @Test
    public void setSessionType_shouldSetCorrespondentRelationInSessionType() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        session.setSessionType(sessionType);

        assertThat(session.getSessionType()).isEqualTo(sessionType);
        assertThat(sessionType.getSessions().contains(session)).isTrue();
    }

}
