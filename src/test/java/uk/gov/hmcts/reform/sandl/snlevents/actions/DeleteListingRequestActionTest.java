package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.DeleteListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class DeleteListingRequestActionTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426655440001";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String HEARING_PART_ID = "c16792d6-696d-4758-871b-30d3e75d2ed4";

    private DeleteListingRequestAction action;
    private DeleteListingRequest dlr;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        dlr = new DeleteListingRequest();
        dlr.setHearingId(createUuid(ID));
        dlr.setUserTransactionId(createUuid(TRANSACTION_ID));
        dlr.setHearingVersion(1L);

        this.action = new DeleteListingRequestAction(dlr,
            hearingRepository,
            entityManager,
            objectMapper);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createUuid(ID));

        Mockito.when(hearingRepository.findById(createUuid(ID))).thenReturn(Optional.of(createHearing()));
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

    @Test(expected = EntityNotFoundException.class)
    public void getAndValidateEntities_throwsExceptionOnNullHearing() {
        Mockito.when(hearingRepository.findById(createUuid(ID))).thenReturn(Optional.empty());

        action.getAndValidateEntities();
    }

    @Test
    public void generateFactMessage_returnsMessageOfCorrectType() {
        action.getAndValidateEntities();
        FactMessage factMessage = action.generateFactMessage();

        assertThat(factMessage.getType()).isEqualTo(RulesService.DELETE_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
    }

    @Test
    public void act_worksProperly() {
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<Hearing> captor = ArgumentCaptor.forClass(Hearing.class);

        Mockito.verify(hearingRepository).save(captor.capture());

        assertThat(captor.getValue().isDeleted()).isEqualTo(true);
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearing",
            dlr.getHearingId(),
            null,
            "delete",
            "create",
            0)
        );

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            createUuid(HEARING_PART_ID),
            null,
            "delete",
            "create",
            0)
        );

        action.getAndValidateEntities();

        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(dlr.getHearingId());
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());
        hearing.setHearingParts(Arrays.asList(createHearingPart()));
        return hearing;
    }

    private HearingPart createHearingPart() {
        val hearingPart = new HearingPart();
        hearingPart.setId(createUuid(HEARING_PART_ID));
        return hearingPart;
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
