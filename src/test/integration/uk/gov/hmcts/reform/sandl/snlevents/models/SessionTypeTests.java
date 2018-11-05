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

import java.time.Duration;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionTypeTests extends BaseIntegrationModelTest  {
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

        sessionTypeRepository.saveAndFlush(sessionType);

        SessionType savedSessionType = sessionTypeRepository.findById(MAIN_TYPE_CODE).orElse(null);
        Session savedSession = sessionRepository.findById(sessionId).orElse(null);

        assertThat(savedSessionType.getSessions().size()).isEqualTo(1);
        assertThat(savedSession.getSessionType()).isEqualTo(sessionType);
    }

    @Test
    public void addCaseType_shouldSetCorrespondentRelationInCaseType() {
        CaseType caseType = new CaseType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        sessionType.addCaseType(caseType);

        sessionTypeRepository.saveAndFlush(sessionType);

        CaseType savedCaseType = caseTypeRepository.findById(REF_TYPE_CODE).orElse(null);
        SessionType savedSessionType = sessionTypeRepository.findById(MAIN_TYPE_CODE).orElse(null);

        assertThat(savedSessionType.getCaseTypes().size()).isEqualTo(1);
        assertThat(savedCaseType.getSessionTypes().contains(sessionType)).isTrue();
    }

    @Test
    public void addHearingType_shouldSetCorrespondentRelationInHearingType() {
        HearingType hearingType = new HearingType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        sessionType.addHearingType(hearingType);

        sessionTypeRepository.saveAndFlush(sessionType);

        HearingType savedHearingType = hearingTypeRepository.findById(REF_TYPE_CODE).orElse(null);
        SessionType savedSessionType = sessionTypeRepository.findById(MAIN_TYPE_CODE).orElse(null);

        assertThat(savedSessionType.getHearingTypes().size()).isEqualTo(1);
        assertThat(savedHearingType.getSessionTypes().contains(sessionType)).isTrue();
    }
}
