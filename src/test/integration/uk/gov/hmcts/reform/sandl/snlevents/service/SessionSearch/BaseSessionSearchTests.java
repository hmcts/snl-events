package uk.gov.hmcts.reform.sandl.snlevents.service.SessionSearch;

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

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public class BaseSessionSearchTests extends BaseIntegrationTest {

    protected static final String SESSION_TYPE_CODE = "small-trial";
    private final SessionType SMALL_TRIAL_SESSION_TYPE = new SessionType(SESSION_TYPE_CODE, "ST");

    private final CaseType CASE_TYPE = new CaseType("small-claims", "SC");
    private final HearingType HEARING_TYPE = new HearingType("trial", "Trial");

    protected static final Duration HALF_HOUR = Duration.ofMinutes(30);
    protected static final Duration ONE_HOUR = Duration.ofMinutes(60);
    protected static final Duration TWO_HOUR = Duration.ofMinutes(120);

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
        session.setSessionType(SMALL_TRIAL_SESSION_TYPE);
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
        hearing.setCaseType(CASE_TYPE);
        hearing.setHearingType(HEARING_TYPE);
        hearing.setDuration(duration);
        hearing.setMultiSession(isMultisession);

        return hearing;
    }
}
