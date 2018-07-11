package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class HearingPartServiceTests {

    @Before
    public void init() {
        when(hearingPartRepository.save(any(HearingPart.class))).then(returnsFirstArg());
    }

    @TestConfiguration
    static class HearingPartServiceTestContextConfiguration {
        @Bean
        public HearingPartService hearingPartService() {
            return new HearingPartService();
        }
    }

    @Autowired
    HearingPartService hearingPartService;

    @MockBean
    HearingPartRepository hearingPartRepository;

    @MockBean
    UserTransactionService userTransactionService;

    @MockBean
    SessionRepository sessionRepository;

    @MockBean
    private RulesService rulesService;

    @MockBean
    private FactsMapper factsMapper;

    @MockBean
    private ObjectMapper objectMapper;

    @Test
    public void getAllHearingParts_returnsHearingPartsFromRepository() {
        when(hearingPartRepository.findAll()).thenReturn(createHearingParts());
        List<HearingPart> hearingParts = hearingPartService.getAllHearingParts();

        assertThat(hearingParts.get(0)).isEqualTo(hearingParts.get(0));
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
    public void assignHearingPartToSessionWithTransaction_assignsHearingPartToSession_whenTheresNoTransactionInProgress() throws IOException {
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
    public void assignHearingPartToSessionWithTransaction_indicatesConflict_whenTransactionIsInProgress() throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session exists
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(createSession());
        when(hearingPartRepository.findOne(any(UUID.class))).thenReturn(createHearingPart());
        //there's transaction in progress
        when(userTransactionService.isAnyBeingTransacted(any(UUID.class), any(UUID.class), any(UUID.class))).thenReturn(true);

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    @Test
    public void assignHearingPartToSessionWithTransaction_indicatesConflict_whenTargetSessionDoesNotExist() throws IOException {
        UserTransaction transaction = createUserTransaction();
        when(userTransactionService.transactionConflicted(any(UUID.class))).thenReturn(transaction);
        //target session doesn't exist
        when(sessionRepository.findOne(any(UUID.class))).thenReturn(null);

        UserTransaction returnedTransaction = hearingPartService.assignHearingPartToSessionWithTransaction(
            createUuid(), createHearingPartSessionRelationship()
        );

        assertThat(returnedTransaction).isEqualTo(transaction);
    }

    private UserTransaction createUserTransaction() {
        return new UserTransaction();
    }

    private List<HearingPart> createHearingParts() {
        return new ArrayList<>(Arrays.asList(createHearingPart()));
    }

    private HearingPart createHearingPart() {
        HearingPart hearingPart = new HearingPart();

        return hearingPart;
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
