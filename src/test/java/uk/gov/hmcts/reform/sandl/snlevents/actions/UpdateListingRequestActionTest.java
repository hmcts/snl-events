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
import uk.gov.hmcts.reform.sandl.snlevents.model.db.*;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UpdateListingRequestActionTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426655440001";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String CASE_TYPE_CODE = "case-type-code";
    private static final String HEARING_TYPE_CODE = "hearing-type-code";
    private static final HearingType HEARING_TYPE = new HearingType(HEARING_TYPE_CODE, "hearing-type-description");
    private static final CaseType CASE_TYPE = new CaseType(CASE_TYPE_CODE, "case-type-description");

    private StatusesMock statusesMock = new StatusesMock();
    private UpdateListingRequestAction action;
    private UpdateListingRequest ulr;
    private Hearing hearing = new Hearing();
    private Session session = new Session();

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
        session.setId(UUID.randomUUID());

        hearing.setId(createUuid(ID));
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());

        Mockito.when(hearingRepository.findOne(createUuid(ID))).thenReturn(hearing);
        when(hearingRepository.save(Matchers.any(Hearing.class))).thenReturn(hearing);
        when(caseTypeRepository.findOne(eq(CASE_TYPE_CODE))).thenReturn(CASE_TYPE);
        when(hearingTypeRepository.findOne(eq(HEARING_TYPE_CODE))).thenReturn(HEARING_TYPE);

        ulr = new UpdateListingRequest();
        ulr.setId(createUuid(ID));
        ulr.setCaseNumber("cn");
        ulr.setUserTransactionId(createUuid(TRANSACTION_ID));
        ulr.setCaseTypeCode(CASE_TYPE_CODE);
        ulr.setHearingTypeCode(HEARING_TYPE_CODE);
        ulr.setNumberOfSessions(2);

        this.action = new UpdateListingRequestAction(
            ulr,
            entityManager,
            objectMapper,
            hearingTypeRepository,
            caseTypeRepository,
            hearingRepository,
            hearingPartRepository,
            statusesMock.statusConfigService
        );
    }

    @Test
    public void action_isInstanceOfProperClasses() {
        assertThat(action).isInstanceOf(Action.class);
        assertThat(action).isInstanceOf(RulesProcessable.class);
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids).isEqualTo(new UUID[] {createUuid(ID)});
    }

    @Test
    public void getUserTransactionId_returnsCorrectId() {
        UUID id = action.getUserTransactionId();

        assertThat(id).isEqualTo(createUuid(TRANSACTION_ID));
    }

    @Test
    public void generateFactMessage_returnsMessageOfCorrectType() {
        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));

        action.getAndValidateEntities();
        List<FactMessage> factMessages = action.generateFactMessages();

        assertThat(factMessages.size()).isEqualTo(hearing.getHearingParts().size());
        assertThat(factMessages.get(0).getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessages.get(0).getData()).isNotNull();
    }

    @Test
    public void act_worksProperly_WhenNotChangingNumberOfSessions() {
        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);

        Mockito.verify(hearingRepository).save(captor.capture());

        assertThat(captor.getValue().getCaseNumber()).isEqualTo(ulr.getCaseNumber());
        assertThat(captor.getValue().getNumberOfSessions()).isEqualTo(ulr.getNumberOfSessions());
    }

    @Test
    public void act_worksProperly_WhenChangingNumberOfSessions_ForUnlistedRequest() {
        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);

        Mockito.verify(hearingRepository).save(captor.capture());

        assertThat(captor.getValue().getCaseNumber()).isEqualTo(ulr.getCaseNumber());
        assertThat(captor.getValue().getNumberOfSessions()).isEqualTo(ulr.getNumberOfSessions());
    }

    @Test
    public void act_worksProperly_whenChangingNumberOfSessions_ForListedRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Cannot increase number of sessions for a listed request!");

        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void act_worksProperly_whenAmendingRequestOnOrBeforeTodaysDate_ForListedRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Cannot amend listing request if starts on or before today's date!");

        session.setStart(OffsetDateTime.now());
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void act_worksProperly_whenTooManySessions_ForSingleSessionRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Single-session hearings cannot have more than 2 sessions!");

        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
        hearing.setMultiSession(false);
        hearing.setNumberOfSessions(1);
        hearing.setHearingParts(createHearingParts(session, 1));
        ulr.setNumberOfSessions(3);

        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void act_worksProperly_whenNotEnoughSessions_ForMultiSessionRequest_shouldThrowException() {
        expectedException.expect(SnlEventsException.class);
        expectedException.expectMessage("Multi-session hearings cannot have less than 2 sessions!");

        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));
        ulr.setNumberOfSessions(1);

        action.getAndValidateEntities();
        action.act();
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        session.setStart(OffsetDateTime.now().plusDays(1));
        hearing.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Unlisted));
        hearing.setMultiSession(true);
        hearing.setNumberOfSessions(2);
        hearing.setHearingParts(createHearingParts(session, 2));

        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearing",
            ulr.getId(),
            Mockito.any(),
            "update",
            "update",
            0)
        );

        hearing.getHearingParts().forEach(hp -> expectedTransactionData.add(new UserTransactionData("hearingPart",
            hp.getId(),
            null,
            "lock",
            "unlock",
            0))
        );

        action.getAndValidateEntities();

        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }

    private List<HearingPart> createHearingParts(Session session, int numberOfSessions) {
        List<HearingPart> hearingParts = new ArrayList<>();

        for (int i = 0; i < numberOfSessions; i++) {
            HearingPart hearingPart = new HearingPart();
            hearingPart.setId(UUID.randomUUID());
            hearingPart.setHearing(hearing);
            hearingPart.setSession(session);
            hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(Status.Listed));
            hearingParts.add(hearingPart);
        }

        return hearingParts;
    }
}
