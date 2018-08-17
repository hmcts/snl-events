package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.ReferenceDataValidator;

import java.time.Duration;
import java.util.UUID;
import javax.transaction.Transactional;

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
    public void addSessionType_shouldSetCorrespondentRelationInSessionType() {
        SessionType sessionType = new SessionType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        session.addSessionType(sessionType);

        new ReferenceDataValidator<Session, SessionType, UUID, String>()
            .save(sessionRepository, session)
            .fetchAgain(sessionId, REF_TYPE_CODE, sessionTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(Session::getSessionTypes, SessionType::getSessions);
    }

}
