package uk.gov.hmcts.reform.sandl.snlevents.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionRepositoryTest extends BaseIntegrationTestWithFakeRules {

    public final String judgeUserName = "djcope";

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    PersonRepository personRepository;

    @Test
    public void sessions_createdSessionIsRetrievable() throws Exception {

        Person djCope = getJudgeFromDb(judgeUserName);

        int sessionCount = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(
            january2017(), january2019(), judgeUserName
        ).size();

        Session session = createSession(djCope, january2018());
        sessionRepository.save(session);

        int sessionCountAfter = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(
            january2017(), january2019(), judgeUserName
        ).size();

        assertThat(sessionCountAfter).isEqualTo(sessionCount + 1);
    }

    private Person getJudgeFromDb(String username) {
        return personRepository.findAll().stream().filter(p -> username.equals(p.getUsername()))
            .findFirst()
            .get();
    }

    private Session createSession(Person djCope, OffsetDateTime start) {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setPerson(djCope);
        session.setStart(start);
        session.setDuration(Duration.ofMinutes(30));
        return session;
    }

    private OffsetDateTime january2017() {
        return january(2017);
    }

    private OffsetDateTime january2018() { return january(2018); }

    private OffsetDateTime january2019() {
        return january(2019);
    }

    private OffsetDateTime january(int year) {
        return OffsetDateTime.of(LocalDateTime.of(year, 1, 1, 1, 1),
            ZoneOffset.ofHoursMinutes(1, 0));
    }

}