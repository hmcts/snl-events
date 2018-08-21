package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
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
    UserTransactionService userTransactionService;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RulesService rulesService;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    S2SAuthenticationService s2SAuthenticationService;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private FactsMapper factsMapper;
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ObjectMapper objectMapper;

    @Before
    public void init() {
        when(hearingPartRepository.save(any(HearingPart.class))).then(returnsFirstArg());
    }

    @Test
    public void getAllHearingParts_returnsHearingPartsFromRepository() {
        when(hearingPartRepository.findAll()).thenReturn(createHearingParts());
        List<HearingPart> hearingParts = hearingPartService.getAllHearingParts();

        assertThat(hearingParts.get(0)).isEqualTo(hearingParts.get(0));
    }

    @Test
    public void getAllHearingPartsThat_whenAreListedIsFalse_returnsHearingPartsWithNoSessionAssignedFromRepository() {
        when(hearingPartRepository.findBySessionIsNull()).thenReturn(createHearingParts());
        List<HearingPart> hearingParts = hearingPartService.getAllHearingPartsThat(false);

        assertThat(hearingParts.get(0)).isEqualTo(hearingParts.get(0));
        assertThat(hearingParts.get(0).getSession()).isNull();
    }

    @Test
    public void getAllHearingPartsThat_whenAreListedIsTrue_returnsHearingPartsWithSessionAssignedFromRepository() {
        when(hearingPartRepository.findBySessionIsNotNull()).thenReturn(Arrays.asList(createHearingPartWithSession()));
        List<HearingPart> hearingParts = hearingPartService.getAllHearingPartsThat(true);

        assertThat(hearingParts.get(0)).isEqualTo(hearingParts.get(0));
        assertThat(hearingParts.get(0).getSession()).isNotNull();
    }

    @Test
    public void save_savesHearingPartToRepository() {
        HearingPart hearingPart = createHearingPart();
        HearingPart savedHearingPart =  hearingPartService.save(hearingPart);

        assertThat(savedHearingPart).isEqualTo(hearingPart);
    }

    @Test
    public void assignWithTransaction_startsTransaction() throws JsonProcessingException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.startTransaction(any(UUID.class), any(List.class))).thenReturn(transaction);

        UserTransaction returnedTransaction = hearingPartService.assignWithTransaction(
            createHearingPart(), createUuid(), createSession(), createSession()
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

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_indicatesConflict_whenTransactionIsInProgress()
        throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        //there's transaction in progress
        when(userTransactionService.isAnyBeingTransacted(any(UUID.class), any(UUID.class), any(UUID.class)))
            .thenReturn(true);

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
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
    public void deleteHearingPart_setsDeletedFlagOnHearingPartEntity() {
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());

        val returnedHearingPart = hearingPartService.deleteHearingPart(UUID.randomUUID());

        assertThat(returnedHearingPart.isDeleted()).isEqualTo(true);
    }

    private UserTransaction createUserTransaction() {
        return new UserTransaction();
    }

    private List<HearingPart> createHearingParts() {
        return Arrays.asList(createHearingPart());
    }

    private HearingPart createHearingPart() {
        return new HearingPart();
    }

    private HearingPart createHearingPartWithSession() {
        HearingPart hp = new HearingPart();
        hp.setSession(createSession());

        return hp;
    }

    private Session createSession() {
        return new Session();
    }

    private UUID createUuid() {
        return UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    }

    private HearingPartSessionRelationship createHearingPartSessionRelationship() {
        return new HearingPartSessionRelationship();
    }
}
