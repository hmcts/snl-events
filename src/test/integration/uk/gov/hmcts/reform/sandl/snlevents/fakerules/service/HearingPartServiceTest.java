package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.FactMessageService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class HearingPartServiceTest extends BaseIntegrationTestWithFakeRules {

    @MockBean
    FactMessageService factMessageService;

    @MockBean
    RulesService rulesService;

    @Autowired
    HearingPartService hearingPartService;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    UserTransactionService userTransactionService;

    @Test
    public void assignHearingPartToSessionWithTransaction_shouldWorkInTransactionalManner() throws Exception {
        HearingPart savedHearingPart = hearingPartRepository.save(createHearingPart(UUID.randomUUID()));
        assertThat(savedHearingPart.getSessionId()).isNull();

        Session savedSession = sessionRepository.save(createSession(UUID.randomUUID(), Duration.ofMinutes(30)));

        HearingPartSessionRelationship hearingPartSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID()
        );

        UserTransaction ut = hearingPartService.assignHearingPartToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingPartSessionRelationship);

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
        ut = userTransactionService.commit(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);

        HearingPart hearingPartAfterAssignment = hearingPartRepository.findOne(savedHearingPart.getId());

        assertThat(hearingPartAfterAssignment.getSessionId()).isEqualTo(savedSession.getId());
    }

    private OffsetDateTime january2018() {
        return january(2018);
    }

    private OffsetDateTime january(int year) {
        return OffsetDateTime.of(LocalDateTime.of(year, 1, 1, 1, 1),
            ZoneOffset.ofHoursMinutes(1, 0));
    }


    private Session createSession(UUID uuid, Duration duration) {
        Session session = new Session();
        session.setId(uuid);
        session.setStart(january2018());
        session.setDuration(duration);
        session.setCaseType("SCLAIMS");

        return session;
    }

    private HearingPart createHearingPart(UUID hearingPartId) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(hearingPartId);
        hearingPart.setCreatedAt(january2018());
        hearingPart.setDuration(Duration.ofMinutes(30));
        hearingPart.setCaseType("SCLAIMS");
        hearingPart.setCaseNumber("case number");
        hearingPart.setCaseTitle("case title");

        return hearingPart;
    }

    private HearingPartSessionRelationship createRelationship(UUID sessionUuid, UUID userTransactionId) {
        HearingPartSessionRelationship hearingPartSessionRelationship = new HearingPartSessionRelationship();
        hearingPartSessionRelationship.setSessionId(sessionUuid);
        hearingPartSessionRelationship.setStart(january2018());
        hearingPartSessionRelationship.setUserTransactionId(userTransactionId);

        return hearingPartSessionRelationship;
    }
}
