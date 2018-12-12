package uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendScheduledListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
public class AmendScheduledListingActionTest {
    private static final OffsetDateTime dateTime = OffsetDateTime.now();
    private static final OffsetDateTime START_TIME = dateTime.withHour(10).withMinute(0);

    private static final UUID TRANSACTION_ID = UUID.randomUUID();
    private static final UUID HEARING_PART_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440003");
    private static final Long HEARING_VERSION = 1L;

    private AmendScheduledListingAction action;
    private AmendScheduledListingRequest request;
    private HearingPart unlistedHearingPart;
    private HearingPart listedHearingPart;
    private Hearing hearing;
    private Integer startTimeHour = 12;

    @Mock
    private HearingPartRepository hearingPartRepository;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        hearing = new Hearing();
        hearing.setCaseType(new CaseType("", ""));
        hearing.setHearingType(new HearingType("", ""));

        request = new AmendScheduledListingRequest(TRANSACTION_ID, HEARING_PART_ID,
            HEARING_VERSION, LocalTime.of(startTimeHour, 0));

        unlistedHearingPart = HearingPart.builder()
            .id(UUID.randomUUID())
            .start(START_TIME)
            .hearingId(UUID.randomUUID())
            .status(StatusConfig.builder().status(Status.Unlisted).build())
            .build();

        listedHearingPart = unlistedHearingPart.toBuilder()
            .status(StatusConfig.builder().status(Status.Listed).build())
            .build();

        Mockito.when(hearingRepository.findOne(any(UUID.class)))
            .thenReturn(hearing);
    }

    @Test(expected = SnlEventsException.class)
    public void getAndValidateEntities_ThrowsExceptionForUnListedHearing() {
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(unlistedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);

        action.getAndValidateEntities();
    }

    @Test
    public void act_setsProperStartTime() {
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(listedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<HearingPart> argument = ArgumentCaptor.forClass(HearingPart.class);
        Mockito.verify(hearingPartRepository, times(1)).save(argument.capture());
        Mockito.verify(hearingPartRepository, times(1)).save(listedHearingPart);
        assertThat(argument.getValue().getStart()).isEqualTo(START_TIME.withHour(startTimeHour));
    }

    @Test
    public void act_detachesProperMethod() {
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(listedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);
        action.getAndValidateEntities();
        action.act();

        Mockito.verify(entityManager, times(1)).detach(listedHearingPart);
        Mockito.verify(entityManager, times(1)).detach(hearing);
    }

    @Test
    public void generateUserTransactionData_ReturnsProperData() {
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(listedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);
        action.getAndValidateEntities();

        UserTransactionData utd = action.generateUserTransactionData().get(0);
        assertThat(utd.getEntityId()).isEqualTo(listedHearingPart.getId());
        assertThat(utd.getEntity()).isEqualTo("hearingPart");
        assertThat(utd.getAction()).isEqualTo("update");
        assertThat(utd.getCounterAction()).isEqualTo("update");
    }

    @Test
    public void getUserTransactionId_ReturnsUserTransactionIdFromRequest() {
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(listedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);
        action.getAndValidateEntities();

        UUID uuid = action.getUserTransactionId();
        assertThat(uuid).isEqualTo(request.getUserTransactionId());
    }

    @Test
    public void generateFactMessage_ReturnsMessageOfProperType() {
        listedHearingPart.setHearing(hearing);
        Mockito.when(hearingPartRepository.findOne(any(UUID.class)))
            .thenReturn(listedHearingPart);

        action = new AmendScheduledListingAction(request, hearingPartRepository,
            entityManager, objectMapper, hearingRepository);
        action.getAndValidateEntities();

        String factMessageType = action.generateFactMessages().get(0).getType();
        assertThat(factMessageType).isEqualTo(RulesService.UPSERT_HEARING_PART);
    }
}
