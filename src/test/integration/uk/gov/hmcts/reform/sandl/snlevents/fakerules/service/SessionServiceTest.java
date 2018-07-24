package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.FactMessageService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionServiceTest extends BaseIntegrationTestWithFakeRules {

    @MockBean
    FactMessageService factMessageService;

    @MockBean
    RulesService rulesService;

    @Autowired
    SessionService sessionService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    UserTransactionService userTransactionService;

    @Test
    public void saveWithTransaction_shouldSaveTheSessionInTransactionalManner() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        Session session = createSession(sessionUuid, Duration.ofMinutes(30));
        UpsertSession us = createUpserSession(session, Duration.ofMinutes(30), UUID.randomUUID());

        UserTransaction ut = sessionService.saveWithTransaction(us);
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

        ut = userTransactionService.commit(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);

        session = sessionRepository.findOne(sessionUuid);
        assertThat(session).isNotNull();
    }

    @Test
    public void rollback_shouldRevertTheChanges() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        Session session = createSession(sessionUuid, Duration.ofMinutes(30));
        UpsertSession us = createUpserSession(session, Duration.ofMinutes(30), UUID.randomUUID());

        UserTransaction ut = sessionService.saveWithTransaction(us);
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
        assertThat(sessionRepository.findOne(sessionUuid)).isEqualTo(session);

        ut = userTransactionService.rollback(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.ROLLEDBACK);

        session = sessionRepository.findOne(sessionUuid);
        assertThat(session).isNull();
    }

    private Session createSession(UUID uuid, Duration duration) {
        Session session = new Session();
        session.setId(uuid);
        session.setStart(january2018());
        session.setDuration(duration);
        session.setCaseType("SCLAIMS");

        return session;
    }

    private UpsertSession createUpserSession(Session session, Duration duration, UUID uuid) {
        UpsertSession us = new UpsertSession();
        us.setUserTransactionId(uuid);
        us.setId(session.getId());
        us.setDuration(duration);
        us.setPersonId(session.getPerson() == null ? null : session.getPerson().getId().toString());
        us.setRoomId(session.getRoom() == null ? null : session.getRoom().getId().toString());
        us.setCaseType(session.getCaseType());
        us.setStart(session.getStart());

        return us;
    }

    private OffsetDateTime january2018() {
        return january(2018);
    }

    private OffsetDateTime january(int year) {
        return OffsetDateTime.of(LocalDateTime.of(year, 1, 1, 1, 1),
            ZoneOffset.ofHoursMinutes(1, 0));
    }
}
