package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CreateListingRequestActionTest {
    private static final String HEARING_PART_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String HEARING_PART_ID2 = "123e4567-e89b-12d3-a456-426655440001";
    private static final String HEARING_ID = "38692091-8165-43a5-8c63-977723a77228";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";
    private StatusesMock statusesMock = new StatusesMock();

    private CreateListingRequestAction action;
    private CreateHearingRequest createHearingRequest;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private HearingTypeRepository hearingTypeRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private EntityManager entityManager;

    @Spy
    private HearingMapper hearingMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        this.createHearingRequest = createCreateHearingPart(1);
        this.action = new CreateListingRequestAction(
            createHearingRequest,
            hearingMapper,
            hearingTypeRepository,
            caseTypeRepository,
            hearingRepository,
            hearingPartRepository,
            statusesMock.statusConfigService,
            statusesMock.statusServiceManager,
            entityManager
        );

        when(hearingRepository.save(any(Hearing.class))).thenReturn(createHearing());
        when(hearingMapper.mapToHearingParts(createHearingRequest)).thenReturn(
            Collections.singletonList(createHearingPart(HEARING_PART_ID))
        );
        when(caseTypeRepository.findOne(createHearingRequest.getCaseTypeCode())).thenReturn(new CaseType());
        when(hearingTypeRepository.findOne(createHearingRequest.getHearingTypeCode())).thenReturn(new HearingType());
    }

    @Test
    public void action_isInstanceOfProperClasses() {
        assertThat(action).isInstanceOf(Action.class);
        assertThat(action).isInstanceOf(RulesProcessable.class);
    }

    @Test
    public void act_shouldSetClassFieldsToProperState() {
        action.getAndValidateEntities();
        action.act();

        assertThat(action.hearing.getStatus().getStatus()).isEqualTo(Status.Unlisted);
        assertThat(action.hearingParts.size()).isEqualTo(1);
        action.hearingParts.forEach(hearingPart -> {
            assertThat(hearingPart.getStatus().getStatus()).isEqualTo(Status.Unlisted);
        });
    }

    @Test
    public void act_createMultiSessionWithLessThanTwoNumberOfSessions_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Multi-session hearings cannot have less than 2 sessions!");
        createHearingRequest.setMultiSession(true);
        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void act_createSingleSessionWithMoreThanTwoNumberOfSessions_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Single-session hearings cannot have more than 2 sessions!");
        createHearingRequest.setMultiSession(false);
        createHearingRequest.setNumberOfSessions(2);
        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void act_shouldSetHearingPartSessionIdToNull() {
        action.getAndValidateEntities();
        action.act();
        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
        captor.getValue().forEach(hp -> {
            assertNull(hp.getSessionId());
            assertNull(hp.getSession());
            assertThat(hp.getStatus().getStatus()).isEqualTo(Status.Unlisted);
        });
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids).isEqualTo(new UUID[]{createUuid(HEARING_ID)});
    }

    @Test
    public void getUserTransactionId_returnsCorrectId() {
        UUID id = action.getUserTransactionId();

        assertThat(id).isEqualTo(createUuid(TRANSACTION_ID));
    }

    @Test
    public void generateFactMessage_returnsMessageOfCorrectType() {
        action.getAndValidateEntities();
        action.act();
        List<FactMessage> factMessages = action.generateFactMessages();

        assertThat(factMessages.size()).isEqualTo(1);
        assertThat(factMessages.get(0).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessages.get(0).getData()).isNotNull();
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(
            new UserTransactionData(
                "hearingPart",
                createUuid(HEARING_PART_ID),
                null,
                "create",
                "delete",
                1
            )
        );

        expectedTransactionData.add(
            new UserTransactionData("hearing",
                createUuid(HEARING_ID),
                null,
                "create",
                "delete",
                0
            )
        );

        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    @Test
    public void getUserTransactionData_withMultupleHearingParts_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            createUuid(HEARING_PART_ID),
            null,
            "create",
            "delete",
            1));


        expectedTransactionData.add(new UserTransactionData("hearingPart",
            createUuid(HEARING_PART_ID2),
            null,
            "create",
            "delete",
            1));

        expectedTransactionData.add(new UserTransactionData("hearing",
            createUuid(HEARING_ID),
            null,
            "create",
            "delete",
            0));

        when(hearingMapper.mapToHearingParts(createHearingRequest)).thenReturn(Arrays.asList(
            createHearingPart(HEARING_PART_ID), createHearingPart(HEARING_PART_ID2))
        );
        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private CreateHearingRequest createCreateHearingPart(int numberOfSessions) {
        val chp = new CreateHearingRequest();
        chp.setId(createUuid(HEARING_ID));
        chp.setCaseTypeCode("ct");
        chp.setUserTransactionId(createUuid(TRANSACTION_ID));
        chp.setNumberOfSessions(numberOfSessions);

        return chp;
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(createUuid(HEARING_ID));
        return new Hearing();
    }

    private HearingPart createHearingPart(String hearingPartId) {
        val hp = new HearingPart();
        hp.setId(createUuid(hearingPartId));

        return hp;
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
