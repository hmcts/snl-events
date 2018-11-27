package uk.gov.hmcts.reform.sandl.snlevents.actions.listingrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.*;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import javax.persistence.EntityManager;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UpdateListingRequestActionTest {

    private static final String HEARING_PART_ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String HEARING_ID = "38692091-8165-43a5-8c63-977723a77228";
    private static final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426655440000";
    private StatusesMock statusesMock = new StatusesMock();

    private UpdateListingRequestAction action;
    private UpdateListingRequest updateListingRequest;
    private HearingPart hearingPart;

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
    private ObjectMapper objectMapper;

    @Spy
    private FactsMapper factsMapper;

    @Before
    public void setup() {
        this.updateListingRequest = updateListingRequest(1);
        this.action = new UpdateListingRequestAction(
            updateListingRequest,
            entityManager,
            objectMapper,
            hearingTypeRepository,
            caseTypeRepository,
            hearingRepository,
            hearingPartRepository,
            statusesMock.statusConfigService
        );
        this.hearingPart = createHearingPart(HEARING_PART_ID);

        when(hearingRepository.save(any(Hearing.class))).thenReturn(createHearing());

        when(caseTypeRepository.findOne(updateListingRequest.getCaseTypeCode())).thenReturn(new CaseType());
        when(hearingTypeRepository.findOne(updateListingRequest.getHearingTypeCode())).thenReturn(new HearingType());
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

        expectedTransactionData.add(new UserTransactionData("hearingPart",
            createUuid(HEARING_PART_ID),
            null,
            "create",
            "delete",
            0));

        expectedTransactionData.add(new UserTransactionData("hearing",
            createUuid(HEARING_ID),
            null,
            "lock",
            "unlock",
            0));

        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private UpdateListingRequest updateListingRequest(int numberOfSessions) {
        val uhp = new UpdateListingRequest();
        uhp.setId(createUuid(HEARING_ID));
        uhp.setCaseTypeCode("ct");
        uhp.setUserTransactionId(createUuid(TRANSACTION_ID));
        uhp.setNumberOfSessions(numberOfSessions);

        return uhp;
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
