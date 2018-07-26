package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sandl.snlevents.config.SubscribersConfiguration;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.transaction.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Transactional
public class HearingPartServiceTest extends BaseIntegrationTestWithFakeRules {

    private static final Map<String, List<String>> subscribers;

    static {
        subscribers = new HashMap<>();
        subscribers.put("upsert-hearingPart", Arrays.asList("http://localhost:8191/msg?rulesDefinition=Listings"));
    }

    @MockBean
    SubscribersConfiguration subscribersConfiguration;

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
        when(subscribersConfiguration.getSubscribers()).thenReturn(subscribers);

        stubFor(post(urlEqualTo("/msg?rulesDefinition=Listings"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")));

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

    @Test
    public void assignHearingPartToSessionWithTransaction_shouldReturnConflict() throws Exception {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPartBuilder.withId(UUID.randomUUID()).build());
        Session savedSession = sessionRepository.save(sessionBuilder.build());

        HearingPartSessionRelationship hearingPartSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID()
        );

        UserTransaction ut = hearingPartService.assignHearingPartToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingPartSessionRelationship);

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

        UserTransaction conflictingUt = hearingPartService.assignHearingPartToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingPartSessionRelationship);

        assertThat(conflictingUt.getStatus()).isEqualTo(UserTransactionStatus.CONFLICT);

        userTransactionService.commit(ut.getId());

        UserTransaction nonConflictingUt = hearingPartService.assignHearingPartToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingPartSessionRelationship);

        assertThat(nonConflictingUt.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

    }

    private HearingPartSessionRelationship createRelationship(UUID sessionUuid, UUID userTransactionId) {
        HearingPartSessionRelationship hearingPartSessionRelationship = new HearingPartSessionRelationship();
        hearingPartSessionRelationship.setSessionId(sessionUuid);
        hearingPartSessionRelationship.setStart(OffsetDateTimeHelper.january2018());
        hearingPartSessionRelationship.setUserTransactionId(userTransactionId);

        return hearingPartSessionRelationship;
    }
}
