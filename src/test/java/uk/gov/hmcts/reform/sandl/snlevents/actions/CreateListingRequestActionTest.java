package uk.gov.hmcts.reform.sandl.snlevents.actions;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.CreateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CreateListingRequestActionTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";

    CreateListingRequestAction action;

    CreateHearingPart createHearingPart;

    @Mock
    HearingPartRepository hearingPartRepository;

    @Before
    public void setup() {
        this.createHearingPart = createCreateHearingPart();
        this.action = new CreateListingRequestAction(createHearingPart, hearingPartRepository);
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
        FactMessage factMessage = action.generateFactMessage();

        assertThat(factMessage.getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
    }

    @Test
    public void act_worksProperly() {
        action.act();

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);
        Mockito.verify(hearingPartRepository).save(captor.capture());

        HearingPart expectedHearingPart = new HearingPart();
        expectedHearingPart.setId(createHearingPart.getId());
        expectedHearingPart.setCaseType(createHearingPart.getCaseType());

        assertThat(captor.getValue().getId()).isEqualTo(expectedHearingPart.getId());
        assertThat(captor.getValue().getCaseType()).isEqualTo(expectedHearingPart.getCaseType());
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            createUuid(ID),
            null,
            "create",
            "delete",
            0)
        );

        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private CreateHearingPart createCreateHearingPart() {
        val chp = new CreateHearingPart();
        chp.setId(createUuid(ID));
        chp.setCaseType("ct");
        chp.setUserTransactionId(createUuid(TRANSACTION_ID));

        return chp;
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
