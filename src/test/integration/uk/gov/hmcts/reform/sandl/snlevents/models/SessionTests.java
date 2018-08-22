package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.time.Duration;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionTests extends BaseIntegrationModelTest  {
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    SessionRepository sessionRepository;

    UUID sessionId = UUID.randomUUID();
    Session session;

    @Before
    public void setUp() {
        session = new Session();
        session.setId(sessionId);
        session.setDuration(Duration.ofHours(1));
        session.setStart(OffsetDateTimeHelper.january2018());
    }

    @Test
    public void setSessionType_shouldSetCorrespondentRelationInSessionType() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        session.setSessionType(sessionType);

        sessionRepository.saveAndFlush(session);
        SessionType savedRoomType = sessionTypeRepository.findOne(REF_TYPE_CODE);
        Session savedSession = sessionRepository.findOne(sessionId);

        assertThat(savedSession.getSessionType()).isEqualTo(sessionType);
        assertThat(savedRoomType.getSessions().size()).isEqualTo(1);
    }

}
