package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.SessionAssignmentData;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class HearingPartServiceTest {
    @InjectMocks
    HearingPartService hearingPartService;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    EntityManager entityManager;
    @Mock
    HearingPartRepository hearingPartRepository;
    @Mock
    HearingRepository hearingRepository;
    @Mock
    UserTransactionService userTransactionService;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RulesService rulesService;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    S2SRulesAuthenticationClient s2SRulesAuthenticationClient;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private FactsMapper factsMapper;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ObjectMapper objectMapper;

    @Before
    public void init() {
        when(hearingPartRepository.save(any(HearingPart.class))).then(returnsFirstArg());
        when(hearingRepository.save(any(Hearing.class))).then(returnsFirstArg());
    }

    @Test
    public void getAllHearingParts_returnsHearingPartsFromRepository() {
        List<HearingPart> hearingParts = createHearingParts();
        when(hearingPartRepository.findAll()).thenReturn(hearingParts);
        List<HearingPartResponse> obtainedHearingParts = hearingPartService.getAllHearingParts();

        assertThat(obtainedHearingParts.size()).isEqualTo(hearingParts.size());
    }

    @Test
    public void getAllHearingPartsThat_whenAreListedIsFalse_returnsHearingPartsWithNoSessionAssignedFromRepository() {
        when(hearingPartRepository.findAll()).thenReturn(
            Arrays.asList(createHearingPartAndHearingWithStatus(createStatusConfigUnlisted()),
            createHearingPartAndHearingWithStatus(createStatusConfigListed())));
        List<HearingPartResponse> hearingPartResponses = hearingPartService.getAllHearingPartsThat(false);

        assertThat(hearingPartResponses.size()).isEqualTo(1);
    }

    @Test
    public void getAllHearingPartsThat_whenAreListedIsTrue_returnsHearingPartsWithSessionAssignedFromRepository() {
        when(hearingPartRepository.findAll()).thenReturn(
            Arrays.asList(createHearingPartAndHearingWithStatus(createStatusConfigListed()),
                createHearingPartAndHearingWithStatus(createStatusConfigUnlisted())));
        List<HearingPartResponse> hearingPartResponses = hearingPartService.getAllHearingPartsThat(true);

        assertThat(hearingPartResponses.size()).isEqualTo(1);
    }

    @Test
    public void save_savesHearingPartToRepository() {
        HearingPart hearingPart = createHearingPart();
        HearingPart savedHearingPart =  hearingPartService.save(hearingPart);

        assertThat(savedHearingPart).isEqualTo(hearingPart);
    }

    @Test
    public void assignWithTransaction_startsTransaction() {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.startTransaction(any(UUID.class), any(List.class))).thenReturn(transaction);

        UserTransaction returnedTransaction = hearingPartService.assignWithTransaction(
            createHearing(), createUuid(), createSessions(), "das", Arrays.asList("daa")
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingToSessionWithTransaction_assignsThemSession_whenTheresNoTransactionInProgress()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.rulesProcessed(any(UserTransaction.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findSessionByIdIn(any())).thenReturn(createSessions());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(createHearing());

        UserTransaction returnedTransaction = hearingPartService.assignHearingToSessionWithTransaction(
            createUuid(), createHearingSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test(expected = SnlRuntimeException.class)
    public void assignHearingToSessionWithTransaction_throwException_whenObjectMapperCantConvertHearingPart()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.rulesProcessed(any(UserTransaction.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findSessionByIdIn(any())).thenReturn(createSessions());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(createHearing());

        Mockito.when(objectMapper.writeValueAsString(isA(HearingPart.class)))
            .thenThrow(new JsonProcessingException("") {});

        UserTransaction returnedTransaction = hearingPartService.assignHearingToSessionWithTransaction(
            createUuid(), createHearingSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingToSessionWithTransaction_indicatesConflict_whenTransactionIsInProgress()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findSessionByIdIn(any())).thenReturn(createSessions());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(createHearing());
        //there's transaction in progress
        when(userTransactionService.isAnyBeingTransacted(any(UUID.class), any(UUID.class),
            any(UUID.class)))
            .thenReturn(true);

        UserTransaction returnedTransaction = hearingPartService.assignHearingToSessionWithTransaction(
            createUuid(), createHearingSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingToSessionWithTransaction_indicatesConflict_whenTargetSessionDoesNotExist()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session doesn't exist
        when(sessionRepository.findSessionByIdIn(any())).thenReturn(new ArrayList<>());

        UserTransaction returnedTransaction = hearingPartService.assignHearingToSessionWithTransaction(
            createUuid(), createHearingSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_indicatesConflict_whenTargetSessionDoesNotExist()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session doesn't exist
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(null);

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_assignsThemSession_whenTheresNoTransactionInProgress()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.rulesProcessed(any(UserTransaction.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        when(hearingRepository.findOne(any(UUID.class))).thenReturn(createHearing());

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void findOne_CallsRepo() {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());

        hearingPartService.findOne(UUID.randomUUID());

        verify(hearingPartRepository).findOne(any(UUID.class));
    }

    private UserTransaction createUserTransaction() {
        return new UserTransaction();
    }

    private List<HearingPart> createHearingParts() {
        return Arrays.asList(createHearingPart());
    }

    private HearingPart createHearingPart() {
        HearingPart hp = new HearingPart();
        Hearing h = new Hearing();
        h.addHearingPart(hp);
        h.setStatus(createStatusConfigListed());
        hp.setHearing(h);
        hp.getHearing().setCaseType(new CaseType("code", "desc"));
        hp.getHearing().setHearingType(new HearingType("code", "desc"));
        hp.setSessionId(createUuid());

        return hp;
    }

    private Hearing createHearing() {
        Hearing h = new Hearing();
        h.addHearingPart(createHearingPart());

        return h;
    }

    private Session createSession() {
        Session session = new Session();

        session.setId(UUID.randomUUID());

        return session;
    }

    private StatusConfig createStatusConfigListed() {
        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Listed);
        statusConfig.setCanBeListed(true);
        statusConfig.setCanBeUnlisted(true);
        return statusConfig;
    }

    private StatusConfig createStatusConfigUnlisted() {
        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Unlisted);
        statusConfig.setCanBeListed(true);
        statusConfig.setCanBeUnlisted(false);
        return statusConfig;
    }

    private HearingPart createHearingPartAndHearingWithStatus(StatusConfig status) {
        HearingPart hp = new HearingPart();
        Hearing h = new Hearing();
        h.setStatus(status);
        h.addHearingPart(createHearingPart());
        hp.setHearing(h);

        hp.getHearing().setCaseType(new CaseType("code", "desc"));
        hp.getHearing().setHearingType(new HearingType("code", "desc"));

        return hp;
    }

    private List<Session> createSessions() {
        Session session = new Session();

        session.setId(UUID.randomUUID());

        return Arrays.asList(session);
    }

    private UUID createUuid() {
        return UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    }

    private HearingSessionRelationship createHearingSessionRelationship() {
        HearingSessionRelationship relationship = new HearingSessionRelationship();
        relationship.setSessionsData(Arrays.asList(new SessionAssignmentData()));

        return relationship;
    }

    private HearingPartSessionRelationship createHearingPartSessionRelationship() {
        HearingPartSessionRelationship relationship = new HearingPartSessionRelationship();
        relationship.setSessionData(new SessionAssignmentData());

        return relationship;
    }
}
