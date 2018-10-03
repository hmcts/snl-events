package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest.UpdateListingRequestAction;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

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

    private UpdateListingRequestAction action;
    private UpdateListingRequest ulr;

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

    @Before
    public void setup() {
        ulr = new UpdateListingRequest();
        ulr.setId(createUuid(ID));
        ulr.setCaseNumber("cn");
        ulr.setUserTransactionId(createUuid(TRANSACTION_ID));
        ulr.setCaseTypeCode(CASE_TYPE_CODE);
        ulr.setHearingTypeCode(HEARING_TYPE_CODE);

        this.action = new UpdateListingRequestAction(ulr,
            hearingPartRepository,
            entityManager,
            objectMapper,
            hearingTypeRepository,
            caseTypeRepository);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(createUuid(ID));
        hearingPart.setHearingType(HEARING_TYPE);
        hearingPart.setCaseType(CASE_TYPE);
        Mockito.when(hearingPartRepository.findOne(createUuid(ID))).thenReturn(hearingPart);
        when(hearingPartRepository.save(Matchers.any(HearingPart.class))).thenReturn(hearingPart);
        when(caseTypeRepository.findOne(eq(CASE_TYPE_CODE))).thenReturn(CASE_TYPE);
        when(hearingTypeRepository.findOne(eq(HEARING_TYPE_CODE))).thenReturn(HEARING_TYPE);
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

        assertThat(factMessage.getType()).isEqualTo(RulesService.UPSERT_HEARING_PART);
        assertThat(factMessage.getData()).isNotNull();
    }

    @Test
    public void act_worksProperly() {
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<HearingPart> captor = ArgumentCaptor.forClass(HearingPart.class);

        Mockito.verify(hearingPartRepository).save(captor.capture());

        HearingPart expectedHearingPart = new HearingPart();

        expectedHearingPart.setId(ulr.getId());

        assertThat(captor.getValue().getId()).isEqualTo(expectedHearingPart.getId());
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            ulr.getId(),
            Mockito.any(),
            "update",
            "update",
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
