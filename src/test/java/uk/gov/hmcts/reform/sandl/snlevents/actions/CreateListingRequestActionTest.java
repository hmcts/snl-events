package uk.gov.hmcts.reform.sandl.snlevents.actions;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.CreateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingPartMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CreateListingRequestActionTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";

    private CreateListingRequestAction action;

    private CreateHearingPartRequest createHearingPartRequest;

    private HearingPart hearingPart;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private HearingTypeRepository hearingTypeRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Spy
    private HearingPartMapper hearingPartMapper;

    @Spy
    private FactsMapper factsMapper;

    @Before
    public void setup() {
        this.createHearingPartRequest = createCreateHearingPart();
        this.action = new CreateListingRequestAction(
            createHearingPartRequest,
            hearingPartMapper,
            hearingPartRepository,
            hearingTypeRepository,
            caseTypeRepository
        );
        this.hearingPart = createHearingPart();

        when(hearingPartRepository.save(any(HearingPart.class))).thenReturn(hearingPart);
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
        action.act();
        FactMessage factMessage = action.generateFactMessage();

        assertThat(factMessage.getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
    }

    @Test
    public void generateFactMessage_throwsException() {
        action.act();
        FactMessage factMessage = action.generateFactMessage();

        assertThat(factMessage.getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
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

    private CreateHearingPartRequest createCreateHearingPart() {
        val chp = new CreateHearingPartRequest();
        chp.setId(createUuid(ID));
        chp.setCaseTypeCode("ct");
        chp.setUserTransactionId(createUuid(TRANSACTION_ID));

        return chp;
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setId(createUuid(ID));
        CaseType caseType = new CaseType("ct", "desc");
        hp.setCaseType(caseType);
        hp.setHearingType(new HearingType("code", "description"));

        return hp;
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
