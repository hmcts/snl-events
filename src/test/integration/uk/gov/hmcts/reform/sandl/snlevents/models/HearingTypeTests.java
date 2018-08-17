package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.ReferenceDataValidator;

import java.time.Duration;
import java.util.UUID;
import javax.transaction.Transactional;


@Transactional
public class HearingTypeTests extends BaseIntegrationModelTest  {
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;

    SessionType sessionType = new SessionType(MAIN_TYPE_CODE, MAIN_TYPE_DESCRIPTION);

    @Test
    public void addSession_shouldSetCorrespondentRelationInSession() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setDuration(Duration.ofMinutes(30));
        session.setStart(OffsetDateTimeHelper.january2018());

        sessionType.addSession(session);
        new ReferenceDataValidator<SessionType, Session, String, UUID>()
            .save(sessionTypeRepository, sessionType)
            .fetchAgain(MAIN_TYPE_CODE, sessionId, sessionRepository)
            .verifyThatRelationsBetweenObjAreSet(SessionType::getSessions, Session::getSessionTypes);
    }

    @Test
    public void addCaseType_shouldSetCorrespondentRelationInCaseType() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        sessionType.addCaseType(caseType);

        new ReferenceDataValidator<SessionType, CaseType, String, String>()
            .save(sessionTypeRepository, sessionType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, caseTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(SessionType::getCaseTypes, CaseType::getSessionTypes);
    }

    @Test
    public void addHearingType_shouldSetCorrespondentRelationInHearingType() {
        HearingType hearingType = new HearingType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        sessionType.addHearingType(hearingType);

        new ReferenceDataValidator<SessionType, HearingType, String, String>()
            .save(sessionTypeRepository, sessionType)
            .fetchAgain(MAIN_TYPE_CODE, REF_TYPE_CODE, hearingTypeRepository)
            .verifyThatRelationsBetweenObjAreSet(SessionType::getHearingTypes, HearingType::getSessionTypes);
    }
}
