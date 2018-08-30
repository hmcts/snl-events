package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.DeleteListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class DeleteListingRequestActionTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426655440001";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";

    private DeleteListingRequestAction action;
    private DeleteListingRequest dlr;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        dlr = new DeleteListingRequest();
        dlr.setHearingPartId(createUuid(ID));
        dlr.setUserTransactionId(createUuid(TRANSACTION_ID));
        dlr.setHearingPartVersion(1L);

        this.action = new DeleteListingRequestAction(dlr,
            hearingPartRepository,
            entityManager,
            objectMapper);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createUuid(ID));
        Mockito.when(hearingPartRepository.findOne(createUuid(ID))).thenReturn(hearingPart);
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
        action.getAndValidateEntities();
        FactMessage factMessage = action.generateFactMessage();

        assertThat(factMessage.getType()).isEqualTo(RulesService.DELETE_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
    }

    @Test
    public void act_worksProperly() {
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);

        Mockito.verify(hearingPartRepository).save(captor.capture());

        assertThat(captor.getValue().isDeleted()).isEqualTo(true);
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            dlr.getHearingPartId(),
            Mockito.any(),
            "delete",
            "create",
            0)
        );

        action.getAndValidateEntities();

        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }


    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
