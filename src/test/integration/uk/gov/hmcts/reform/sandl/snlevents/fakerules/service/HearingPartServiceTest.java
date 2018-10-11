package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.io.IOException;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

    @Autowired
    HearingTypeRepository hearingTypeRepository;

    @Autowired
    CaseTypeRepository caseTypeRepository;

    @Autowired
    SessionTypeRepository sessionTypeRepository;

    @Autowired
    EntityManager entityManager;

    @Before
    public void stubRulesServiceResponse() {
        stubFor(post(urlEqualTo("/msg?rulesDefinition=Listings"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")));
        hearingTypeRepository.save(hearingPartBuilder.hearingType);
        caseTypeRepository.save(hearingPartBuilder.caseType);
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_shouldWorkInTransactionalManner() throws Exception {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPartBuilder.withId(UUID.randomUUID()).build());
        assertThat(savedHearingPart.getSessionId()).isNull();

        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID()
        );

        UserTransaction ut = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingSessionRelationship);

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
        ut = userTransactionService.commit(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);

        HearingPart hearingPartAfterAssignment = hearingPartRepository.findOne(savedHearingPart.getId());

        assertThat(hearingPartAfterAssignment.getSessionId()).isEqualTo(savedSession.getId());
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_shouldReturnConflict() throws Exception {
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPartBuilder.withId(UUID.randomUUID()).build());
        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID()
        );

        //do what assignHearingToSessionWithTransaction does but without doing detach
        //detach makes using this method second time throw 'possible non-threadsafe access to session'
        //we want to set started transaction state without detaching hearingPart we are going to use in next step
        UserTransaction ut = hearingPartService.assignWithTransaction(
            savedHearingPart,
            hearingSessionRelationship.getUserTransactionId(),
            savedHearingPart.getSession(),
            savedSession,
            "das", "das"
        );

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

        hearingSessionRelationship.setHearingVersion(savedHearingPart.getVersion());
        UserTransaction conflictingUt = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingSessionRelationship
        );

        assertThat(conflictingUt.getStatus()).isEqualTo(UserTransactionStatus.CONFLICT);

        userTransactionService.commit(ut.getId());

        UserTransaction nonConflictingUt = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearingPart.getId(),
            hearingSessionRelationship
        );

        assertThat(nonConflictingUt.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

    }

    @Test(expected = OptimisticLockingFailureException.class)
    public void assignHearingPartToSessionWithTransaction_throwsException_whenHearingPartIsLocked() throws IOException {
        //GIVEN latest version of HearingPart is 2
        HearingPart savedHearingPart = hearingPartRepository.save(
            hearingPartBuilder.withId(UUID.randomUUID()).withVersion(2L).build()
        );
        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());
        entityManager.flush();

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID()
        );

        //WHEN we try to assign Session to HearingPart with older version
        hearingSessionRelationship.setHearingVersion(1L);
        hearingPartService.assignHearingToSessionWithTransaction(
            savedHearingPart.getId(), hearingSessionRelationship
        );
        //THEN Optimistic Locking prevents us to do so
    }

    private HearingSessionRelationship createRelationship(UUID sessionUuid, UUID userTransactionId) {
        HearingSessionRelationship hearingSessionRelationship = new HearingSessionRelationship();
        hearingSessionRelationship.setSessionId(sessionUuid);
        hearingSessionRelationship.setStart(OffsetDateTimeHelper.january2018());
        hearingSessionRelationship.setUserTransactionId(userTransactionId);
        hearingSessionRelationship.setSessionVersion(0);
        hearingSessionRelationship.setHearingVersion(0);

        return hearingSessionRelationship;
    }

    private SessionType getSessionType() {
        return sessionTypeRepository.findAll()
            .stream()
            .filter(st -> st.getCode().equals("small-claims")).findFirst().get();

    }
}
