package uk.gov.hmcts.reform.sandl.snlevents.fakerules.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.FactMessageService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionRepositoryTest extends BaseIntegrationTestWithFakeRules {

    public static final String JUDGE_USER_NAME = "djcope";

    @MockBean
    public FactMessageService factMessageService;

    @MockBean
    public RulesService rulesService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    SessionTypeRepository sessionTypeRepository;

    @Test
    public void sessions_createdSessionIsRetrievable() {

        Person djCope = getJudgeFromDb(JUDGE_USER_NAME);

        Session session = createSession(djCope, january2018());
        Session createdSession = sessionRepository.save(session);

        List<Session> sessions = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(
            january2017(), january2019(), JUDGE_USER_NAME
        );

        Session expectedSession = sessions
            .stream()
            .filter(s -> session.getId().equals(createdSession.getId()))
            .findFirst()
            .get();

        assertThat(expectedSession).isNotNull();
    }

    private Person getJudgeFromDb(String username) {
        return personRepository.findAll().stream().filter(p -> username.equals(p.getUsername()))
            .findFirst()
            .get();
    }

    private Session createSession(Person djCope, OffsetDateTime start) {
        SessionType sessType = sessionTypeRepository.findAll()
            .stream()
            .filter(st -> st.getCode().equals("small-claims")).findFirst().get();

        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setSessionType(sessType);
        session.setPerson(djCope);
        session.setStart(start);
        session.setDuration(Duration.ofMinutes(30));
        return session;
    }

    private OffsetDateTime january2017() {
        return january(2017);
    }

    private OffsetDateTime january2018() {
        return january(2018);
    }

    private OffsetDateTime january2019() {
        return january(2019);
    }

    private OffsetDateTime january(int year) {
        return OffsetDateTime.of(LocalDateTime.of(year, 1, 1, 1, 1),
            ZoneOffset.ofHoursMinutes(1, 0));
    }

}
