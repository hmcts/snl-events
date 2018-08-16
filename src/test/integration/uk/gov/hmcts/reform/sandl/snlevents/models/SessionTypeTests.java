package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.time.Duration;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionTypeTests extends BaseIntegrationTestWithFakeRules  {
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;

    static final String SESSION_TYPE_CODE = "session_type_code1";
    static final String SESSION_TYPE_DESCRIPTION = "session_type_desc1";
    SessionType sessionType = new SessionType(SESSION_TYPE_CODE, SESSION_TYPE_DESCRIPTION);
    static final String CODE = "code1";
    static final String DESCRIPTION = "desc1";

    @Test
    public void addSession_shouldSetCorrespondentRelationInSession() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setDuration(Duration.ofMinutes(30));
        session.setStart(OffsetDateTimeHelper.january2018());

        sessionType.addSession(session);
        sessionTypeRepository.saveAndFlush(sessionType);

        SessionType savedSessionType = sessionTypeRepository.findOne(SESSION_TYPE_CODE);
        Session savedSession = sessionRepository.findOne(sessionId);

        assertThat(savedSessionType.getDescription()).isEqualTo(SESSION_TYPE_DESCRIPTION);
        assertThat(savedSessionType.getSessions().size()).isEqualTo(1);
        assertThat(savedSession.getSessionTypes().size()).isEqualTo(1);
    }

    @Test
    public void addCaseType_shouldSetCorrespondentRelationInCaseType() {
        CaseType caseType = new CaseType(CODE, DESCRIPTION);
        sessionType.addCaseType(caseType);
        sessionTypeRepository.saveAndFlush(sessionType);

        SessionType savedSessionType = sessionTypeRepository.findOne(SESSION_TYPE_CODE);
        CaseType savedCaseType = caseTypeRepository.findOne(CODE);

        assertThat(savedSessionType.getDescription()).isEqualTo(SESSION_TYPE_DESCRIPTION);
        assertThat(savedSessionType.getCaseTypes().size()).isEqualTo(1);
        assertThat(savedCaseType.getSessionTypes().size()).isEqualTo(1);
    }

    @Test
    public void addHearingType_shouldSetCorrespondentRelationInCaseType() {
        HearingType hearingType = new HearingType(CODE, DESCRIPTION);
        sessionType.addHearingType(hearingType);
        sessionTypeRepository.saveAndFlush(sessionType);

        SessionType savedSessionType = sessionTypeRepository.findOne(SESSION_TYPE_CODE);
        HearingType savedHearingType = hearingTypeRepository.findOne(CODE);

        assertThat(savedSessionType.getDescription()).isEqualTo(SESSION_TYPE_DESCRIPTION);
        assertThat(savedSessionType.getHearingTypes().size()).isEqualTo(1);
        assertThat(savedHearingType.getSessionTypes().size()).isEqualTo(1);
    }

}
