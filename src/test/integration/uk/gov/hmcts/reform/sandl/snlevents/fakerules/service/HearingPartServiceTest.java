package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class HearingPartServiceTest extends BaseIntegrationTestWithFakeRules {

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
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPartBuilder.withId(UUID.randomUUID()).build());
        assertThat(savedHearingPart.getSessionId()).isNull();

        Session savedSession = sessionRepository.save(sessionBuilder.build());

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

    private HearingPartSessionRelationship createRelationship(UUID sessionUuid, UUID userTransactionId) {
        HearingPartSessionRelationship hearingPartSessionRelationship = new HearingPartSessionRelationship();
        hearingPartSessionRelationship.setSessionId(sessionUuid);
        hearingPartSessionRelationship.setStart(OffsetDateTimeHelper.january2018());
        hearingPartSessionRelationship.setUserTransactionId(userTransactionId);

        return hearingPartSessionRelationship;
    }
}
