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
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
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

    private static final String HEARING_PART_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String HEARING_ID = "38692091-8165-43a5-8c63-977723a77228";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";

    private CreateListingRequestAction action;

    private CreateHearingRequest createHearingRequest;

    private HearingPart hearingPart;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingTypeRepository hearingTypeRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Spy
    private HearingMapper hearingMapper;

    @Spy
    private FactsMapper factsMapper;

    @Before
    public void setup() {
        this.createHearingRequest = createCreateHearingPart();
        this.action = new CreateListingRequestAction(
            createHearingRequest,
            hearingMapper,
            hearingTypeRepository,
            caseTypeRepository,
            hearingRepository
        );
        this.hearingPart = createHearingPart();

        when(hearingRepository.save(any(Hearing.class))).thenReturn(createHearing());
        when(hearingMapper.mapToHearingPart(createHearingRequest)).thenReturn(createHearingPart());
        when(caseTypeRepository.findOne(createHearingRequest.getCaseTypeCode())).thenReturn(new CaseType());
        when(hearingTypeRepository.findOne(createHearingRequest.getHearingTypeCode())).thenReturn(new HearingType());
    }

    @Test
    public void action_isInstanceOfProperClasses() {
        assertThat(action).isInstanceOf(Action.class);
        assertThat(action).isInstanceOf(RulesProcessable.class);
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.act();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids).isEqualTo(new UUID[] {createUuid(HEARING_ID), createUuid(HEARING_PART_ID)});
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
            createUuid(HEARING_PART_ID),
            null,
            "create",
            "delete",
            0));

        expectedTransactionData.add(new UserTransactionData("hearing",
            createUuid(HEARING_ID),
            null,
            "create",
            "delete",
            1));

        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private CreateHearingRequest createCreateHearingPart() {
        val chp = new CreateHearingRequest();
        chp.setId(createUuid(HEARING_ID));
        chp.setCaseTypeCode("ct");
        chp.setUserTransactionId(createUuid(TRANSACTION_ID));

        return chp;
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(createUuid(HEARING_ID));
        return new Hearing();
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setId(createUuid(HEARING_PART_ID));

        return hp;
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }
}
