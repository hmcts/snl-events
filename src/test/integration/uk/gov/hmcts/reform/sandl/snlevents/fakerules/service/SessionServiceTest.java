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
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SessionServiceTest extends BaseIntegrationTestWithFakeRules {

    @MockBean
    RulesService rulesService;

    @MockBean
    FactMessageService factMessageService;

    @Autowired
    SessionService sessionService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    UserTransactionService userTransactionService;

    @Test
    public void saveWithTransaction_shouldSaveTheSessionInTransactionalManner() {
        UUID sessionUuid = UUID.randomUUID();
        Session session = sessionBuilder.withId(sessionUuid).build();
        UpsertSession us = upsertSessionBuilder.fromSession(session)
            .withDuration(Duration.ofMinutes(30))
            .withTransactionId(UUID.randomUUID())
            .build();

        UserTransaction ut = sessionService.saveWithTransaction(us);
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

        ut = userTransactionService.commit(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);

        session = sessionRepository.findOne(sessionUuid);
        assertThat(session).isNotNull();
    }

    @Test
    public void rollback_shouldRevertTheChanges() {
        UUID sessionUuid = UUID.randomUUID();
        Session session = sessionBuilder.withId(sessionUuid).build();
        UpsertSession us = upsertSessionBuilder.fromSession(session)
            .withDuration(Duration.ofMinutes(30))
            .withTransactionId(UUID.randomUUID())
            .build();

        UserTransaction ut = sessionService.saveWithTransaction(us);
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
        assertThat(sessionRepository.findOne(sessionUuid)).isEqualTo(session);

        ut = userTransactionService.rollback(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.ROLLEDBACK);

        session = sessionRepository.findOne(sessionUuid);
        assertThat(session).isNull();
    }
}
