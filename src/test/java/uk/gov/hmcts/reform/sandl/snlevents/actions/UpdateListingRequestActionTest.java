package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.UpdateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UpdateListingRequestActionTest {
    private static final String ID = "123e4567-e89b-12d3-a456-426655440001";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String CASE_TYPE_CODE = "case-type-code";
    private static final String HEARING_TYPE_CODE = "previousHearing-type-code";
    private static final HearingType HEARING_TYPE = new HearingType(HEARING_TYPE_CODE,
        "previousHearing-type-description");
    private static final CaseType CASE_TYPE = new CaseType(CASE_TYPE_CODE, "case-type-description");

    private StatusesMock statusesMock = new StatusesMock();
    private UpdateListingRequestAction action;
    private UpdateListingRequest ulr;
    private Hearing previousHearing = new Hearing();

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HearingTypeRepository hearingTypeRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        this.previousHearing = setPreviousHearing();
        this.ulr = setUpdateListingRequest();

        this.action = new UpdateListingRequestAction(
            ulr,
            entityManager,
            objectMapper,
            hearingRepository,
            hearingPartRepository,
            statusesMock.statusConfigService
        );

        Mockito.when(hearingRepository.findOne(createUuid(ID))).thenReturn(previousHearing);
        when(hearingRepository.save(Matchers.any(Hearing.class))).thenReturn(previousHearing);
        when(caseTypeRepository.findOne(eq(CASE_TYPE_CODE))).thenReturn(CASE_TYPE);
        when(hearingTypeRepository.findOne(eq(HEARING_TYPE_CODE))).thenReturn(HEARING_TYPE);
    }

    @Test
    public void action_isInstanceOfProperClasses() {
        assertThat(action).isInstanceOf(Action.class);
        assertThat(action).isInstanceOf(RulesProcessable.class);
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds_whenNot_addingOrRemoving_numberOfSessions_Listed() {
        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(2);

        List<UUID> expectedUuids = new ArrayList<>();

        expectedUuids.add(previousHearing.getId());

        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids.length).isEqualTo(expectedUuids.size());
        assertTrue(Arrays.asList(ids).containsAll(expectedUuids));
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds_when_adding_numberOfSessions_Unlisted() {
        setPreviousHearing(Status.Unlisted, true, 2);
        ulr.setNumberOfSessions(3);

        List<UUID> expectedUuids = new ArrayList<>();
        expectedUuids.add(previousHearing.getId());
        for (int i = 0; i < ulr.getNumberOfSessions() - previousHearing.getNumberOfSessions(); i++) {
            expectedUuids.add(UUID.randomUUID());
        }

        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids.length).isEqualTo(expectedUuids.size());
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds_when_decreasing_numberOfSessions_Listed() {
        setPreviousHearing(Status.Listed, true, 3);
        ulr.setNumberOfSessions(2);

        List<UUID> expectedUuids = new ArrayList<>();
        expectedUuids.add(previousHearing.getId());
        for (int i = 0; i < previousHearing.getNumberOfSessions() - ulr.getNumberOfSessions(); i++) {
            expectedUuids.add(UUID.randomUUID());
            expectedUuids.add(UUID.randomUUID());
        }

        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids.length).isEqualTo(expectedUuids.size());
    }

    @Test
    public void getUserTransactionId_returnsCorrectId() {
        UUID id = action.getUserTransactionId();

        assertThat(id).isEqualTo(createUuid(TRANSACTION_ID));
    }

    @Test
    public void generateFactMessage_returnsMessageOfCorrectType() {
        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(2);

        action.getAndValidateEntities();
        List<FactMessage> factMessages = action.generateFactMessages();

        assertThat(factMessages.size()).isEqualTo(previousHearing.getHearingParts().size());
        assertThat(factMessages.get(0).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessages.get(0).getData()).isNotNull();
    }

    @Test
    public void getUserTransactionData_returnsCorrectData_whenNot_addingOrRemoving_numberOfSessions_Listed() {
        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(2);

        List<HearingPart>  hearingParts = previousHearing.getHearingParts();
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearing",
            ulr.getId(),
            Mockito.any(),
            "update",
            "update",
            2
            )
        );

        hearingParts.forEach(hp -> expectedTransactionData.add(
            new UserTransactionData("session",
                hp.getSessionId(),
                null,
                "lock",
                "unlock",
                0
            )
        ));

        action.getAndValidateEntities();
        action.act();
        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    @Test
    public void getAndValidateEntities_whenChangingNumberOfSessions_forListedRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Cannot increase number of sessions for a listed request!");

        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
    }

    @Test
    public void getAndValidateEntities_whenHearingIsOnOrBeforeTodaysDate_forListedRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Cannot amend listing request if starts on or before today's date!");

        setPreviousHearing(Status.Listed, false, 1);
        previousHearing.getHearingParts().get(0).setStart(OffsetDateTime.now());
        previousHearing.getHearingParts().get(0).getSession().setStart(OffsetDateTime.now());
        ulr.setNumberOfSessions(1);

        action.getAndValidateEntities();
    }

    @Test
    public void getAndValidateEntities_whenTooManySessions_forSingleSessionRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Single-session hearings cannot have more than 2 sessions!");

        setPreviousHearing(Status.Unlisted, false, 1);
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
    }

    @Test
    public void getAndValidateEntities_whenStatusIsIncorrect_forSingleSessionRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Cannot amend listing request that is neither listed or unlisted!");

        setPreviousHearing(Status.Adjourned, false, 1);
        ulr.setNumberOfSessions(1);

        action.getAndValidateEntities();
    }

    @Test
    public void getAndValidateEntities_whenNotEnoughSessions_forMultiSessionRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Multi-session hearings cannot have less than 2 sessions!");

        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(1);

        action.getAndValidateEntities();
    }

    @Test
    public void act_worksProperly_WhenNotChangingNumberOfSessions_forUnlistedSingleSessionRequest() {
        setPreviousHearing(Status.Listed, true, 2);
        ulr.setNumberOfSessions(2);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);
        Mockito.verify(hearingRepository).save(captor.capture());

        assertThat(captor.getValue().getCaseNumber()).isEqualTo(ulr.getCaseNumber());
        assertThat(captor.getValue().getNumberOfSessions()).isEqualTo(ulr.getNumberOfSessions());
        assertThat(captor.getValue().getReservedJudgeId()).isEqualTo(ulr.getReservedJudgeId());
        assertThat(captor.getValue().getCaseNumber()).isEqualTo(ulr.getCaseNumber());
    }

    @Test
    public void act_worksProperly_WhenChangingNumberOfSessions_forUnlistedSingleSessionRequest() {
        setPreviousHearing(Status.Unlisted, true, 2);
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);
        Mockito.verify(hearingRepository).save(captor.capture());

        assertThat(captor.getValue().getCaseNumber()).isEqualTo(ulr.getCaseNumber());
        assertThat(captor.getValue().getNumberOfSessions()).isEqualTo(ulr.getNumberOfSessions());
    }

    @Test
    public void act_worksProperly_whenDecreasingNumberOfSessions_forListedMultiSessionRequest() {
        setPreviousHearing(Status.Listed, true, 4);
        ulr.setNumberOfSessions(2);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
        List<HearingPart> hearingParts = captor.getValue()
            .stream()
            .filter(hp -> hp.getSession() == null)
            .collect(Collectors.toList());

        hearingParts.forEach(hp -> assertThat(hp.getStatus().getStatus()).isEqualTo(Status.Vacated));
    }

    @Test
    public void act_worksProperly_whenDecreasingNumberOfSessions_forUnlistedMultiSessionRequest() {
        setPreviousHearing(Status.Unlisted, true, 4);
        ulr.setNumberOfSessions(2);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());
        List<HearingPart> hearingParts = captor.getValue()
            .stream()
            .filter(hp -> hp.getSession() == null)
            .collect(Collectors.toList());

        hearingParts.forEach(hp -> assertThat(hp.getStatus().getStatus()).isEqualTo(Status.Withdrawn));
    }

    @Test
    public void act_worksProperly_whenIncreasingNumberOfSessions_forUnlistedMultiSessionRequest() {
        setPreviousHearing(Status.Unlisted, true, 2);
        ulr.setNumberOfSessions(4);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());

        assertThat(captor.getValue().size()).isEqualTo(2);
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }

    private Hearing setPreviousHearing() {
        Hearing hearing = new Hearing();
        hearing.setId(createUuid(ID));
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());

        return hearing;
    }

    private void setPreviousHearing(Status status, boolean isMultiSession, int numOfSessions) {
        this.previousHearing.setStatus(statusesMock.statusConfigService.getStatusConfig(status));
        this.previousHearing.setMultiSession(isMultiSession);
        this.previousHearing.setNumberOfSessions(numOfSessions);
        this.previousHearing.setHearingParts(setHearingPartsWithSessions(numOfSessions, status));
    }

    private List<HearingPart> setHearingPartsWithSessions(int numberOfSessions, Status status) {
        List<HearingPart> hearingParts = new ArrayList<>();

        for (int i = 0; i < numberOfSessions; i++) {
            Session session = new Session();
            session.setId(UUID.randomUUID());
            session.setStart(OffsetDateTime.now().plusDays(i + 1));
            HearingPart hearingPart = new HearingPart();
            hearingPart.setId(UUID.randomUUID());
            hearingPart.setHearing(previousHearing);
            hearingPart.setStart(OffsetDateTime.now().plusDays(i + 1));
            hearingPart.setSession(session);
            hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(status));
            hearingParts.add(hearingPart);
        }

        return hearingParts;
    }

    private UpdateListingRequest setUpdateListingRequest() {
        UpdateListingRequest updateListingRequest = new UpdateListingRequest();
        updateListingRequest.setId(createUuid(ID));
        updateListingRequest.setCaseNumber("cn");
        updateListingRequest.setUserTransactionId(createUuid(TRANSACTION_ID));
        updateListingRequest.setCaseTypeCode(CASE_TYPE_CODE);
        updateListingRequest.setHearingTypeCode(HEARING_TYPE_CODE);

        return updateListingRequest;
    }
}
