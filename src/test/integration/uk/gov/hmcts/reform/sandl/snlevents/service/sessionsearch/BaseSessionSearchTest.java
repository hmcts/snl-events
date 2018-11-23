package uk.gov.hmcts.reform.sandl.snlevents.service.sessionsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.transaction.Transactional;

@Transactional
public abstract class BaseSessionSearchTest extends BaseIntegrationTest {

    protected static final String SESSION_TYPE_CODE = "small-trial";
    private final SessionType smallTrialSessionType = new SessionType(SESSION_TYPE_CODE, "ST");

    private final CaseType caseType = new CaseType("small-claims", "SC");
    private final HearingType hearingType = new HearingType("trial", "Trial");

    protected static final Duration HALF_HOUR = Duration.ofMinutes(30);
    protected static final Duration ONE_HOUR = Duration.ofMinutes(60);
    protected static final Duration ONE_AND_HALF_HOUR = Duration.ofMinutes(90);
    protected static final Duration TWO_HOURS = Duration.ofMinutes(120);

    protected static final PageRequest FIRST_PAGE = new PageRequest(0, 10);

    @Autowired
    protected HearingRepository hearingRepository;
    @Autowired
    protected SessionRepository sessionRepository;
    @Autowired
    protected HearingPartRepository hearingPartRepository;

    @Autowired
    protected SessionService sessionService;

    protected Session createSession(Duration duration, UUID uuid, Person person, Room room, OffsetDateTime start) {
        Duration defaultDuration = Duration.ofMinutes(30);

        if (duration == null) {
            duration = defaultDuration;
        }

        UUID id = uuid;
        if (uuid == null) {
            id = UUID.randomUUID();
        }

        OffsetDateTime startTime = start;
        if (start == null) {
            startTime = OffsetDateTime.now();
        }

        Session session = new Session();
        session.setId(id);
        session.setSessionType(smallTrialSessionType);
        session.setDuration(duration);
        session.setStart(startTime);
        session.setPerson(person);
        session.setRoom(room);

        return session;
    }

    protected Hearing createHearing(Duration duration, UUID uuid, Boolean isMultisession) {
        UUID id = uuid;
        if (uuid == null) {
            id = UUID.randomUUID();
        }

        Hearing hearing = new Hearing();
        hearing.setId(id);
        hearing.setCaseType(caseType);
        hearing.setHearingType(hearingType);
        hearing.setDuration(duration);
        hearing.setMultiSession(isMultisession);

        return hearing;
    }
}
