package uk.gov.hmcts.reform.sandl.snlevents.fakerules.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.io.IOException;
import java.util.Arrays;
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
    HearingRepository hearingRepository;

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
        Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearing(hearing);
        hearing.addHearingPart(hearingPart);
        hearing.setCaseType(getCaseType());
        hearing.setHearingType(getHearingType());
        hearing.setNumberOfSessions(1);
        hearing.setMultiSession(false);

        Hearing savedHearing = hearingRepository.save(hearing);
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPart);
        assertThat(savedHearing.getHearingParts().get(0).getSessionId()).isNull();

        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID(), savedHearing.getId()
        );

        UserTransaction ut = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearing.getId(),
            hearingSessionRelationship);

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);
        ut = userTransactionService.commit(ut.getId());
        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.COMMITTED);

        HearingPart hearingPartAfterAssignment = hearingPartRepository.findOne(hearingPart.getId());

        assertThat(hearingPartAfterAssignment.getSessionId()).isEqualTo(savedSession.getId());
    }

    @Test
    public void assignHearingToSessionWithTransaction_shouldReturnConflict() throws Exception {
        Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearing(hearing);
        hearing.addHearingPart(hearingPart);
        hearing.setCaseType(getCaseType());
        hearing.setHearingType(getHearingType());
        hearing.setNumberOfSessions(1);
        hearing.setMultiSession(false);

        Hearing savedHearing = hearingRepository.save(hearing);
        HearingPart savedHearingPart = hearingPartRepository.save(hearingPart);
        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID(), UUID.randomUUID()
        );

        //do what assignHearingToSessionWithTransaction does but without doing detach
        //detach makes using this method second time throw 'possible non-threadsafe access to session'
        //we want to set started transaction state without detaching hearingPart we are going to use in next step
        UserTransaction ut = hearingPartService.assignWithTransaction(
            savedHearing,
            hearingSessionRelationship.getUserTransactionId(),
            Arrays.asList(savedSession),
            "das", Arrays.asList("das")
        );

        assertThat(ut.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

        hearingSessionRelationship.setHearingVersion(savedHearing.getVersion());
        UserTransaction conflictingUt = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearing.getId(),
            hearingSessionRelationship
        );

        assertThat(conflictingUt.getStatus()).isEqualTo(UserTransactionStatus.CONFLICT);

        userTransactionService.commit(ut.getId());

        UserTransaction nonConflictingUt = hearingPartService.assignHearingToSessionWithTransaction(
            savedHearing.getId(),
            hearingSessionRelationship
        );

        assertThat(nonConflictingUt.getStatus()).isEqualTo(UserTransactionStatus.STARTED);

    }

    @Test(expected = OptimisticLockingFailureException.class)
    public void assignHearingPartToSessionWithTransaction_throwsException_whenHearingPartIsLocked() throws IOException {
        //GIVEN latest version of HearingPart is different than newer one

        Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearing.addHearingPart(hearingPart);
        hearing.setCaseType(getCaseType());
        hearing.setHearingType(getHearingType());
        hearing.setNumberOfSessions(1);
        hearing.setMultiSession(false);

        Hearing savedHearing = hearingRepository.save(hearing);
        Session savedSession = sessionRepository.save(sessionBuilder.withSessionType(getSessionType()).build());
        entityManager.flush();

        HearingSessionRelationship hearingSessionRelationship = createRelationship(
            savedSession.getId(), UUID.randomUUID(), UUID.randomUUID()
        );

        //WHEN we try to assign Session to HearingPart with older version
        hearingSessionRelationship.setHearingVersion(1L);
        hearingPartService.assignHearingToSessionWithTransaction(
            savedHearing.getId(), hearingSessionRelationship
        );
        //THEN Optimistic Locking prevents us to do so
    }

    private HearingSessionRelationship createRelationship(UUID sessionUuid, UUID hearingId, UUID userTransactionId) {
        HearingSessionRelationship hearingSessionRelationship = new HearingSessionRelationship();
        hearingSessionRelationship.setSessionsData(Arrays.asList(new SessionAssignmentData(sessionUuid, 0)));
        hearingSessionRelationship.setStart(OffsetDateTimeHelper.january2018());
        hearingSessionRelationship.setUserTransactionId(userTransactionId);
        hearingSessionRelationship.setHearingVersion(0);
        hearingSessionRelationship.setHearingId(hearingId);

        return hearingSessionRelationship;
    }

    private SessionType getSessionType() {
        return sessionTypeRepository.findAll()
            .stream()
            .filter(st -> st.getCode().equals("small-claims")).findFirst().get();

    }

    private HearingType getHearingType() {
        return hearingTypeRepository.findAll()
            .stream()
            .filter(st -> st.getCode().equals("K-ASAJ")).findFirst().get();

    }

    private CaseType getCaseType() {
        return caseTypeRepository.findAll()
            .stream()
            .filter(st -> st.getCode().equals("K-Fast Track")).findFirst().get();

    }
}
