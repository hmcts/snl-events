package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import lombok.var;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpdateListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingPartController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class HearingPartControllerTest {
    public static final String CASE_TYPE_CODE = "type";
    public static final CaseType CASE_TYPE = new CaseType(CASE_TYPE_CODE,"case-type-desc");
    public static final String CASE_NUMBER = "90";
    public static final String TITLE = "title";
    public static final String HEARING_TYPE_CODE = "hearing-type-code";
    public static final HearingType HEARING_TYPE =  new HearingType(HEARING_TYPE_CODE, "hearing-type-desc");
    public static final String URL = "/hearing-part";
    public static final String URL_IS_LISTED_FALSE = "/hearing-part?isListed=false";
    public static final String COMMUNICATION_FACILITATOR = "Interpreter";
    public static final UUID RESERVED_JUDGE_ID = UUID.randomUUID();

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private HearingPartRepository hearingPartRepository;

    @MockBean
    private HearingRepository hearingRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ActionService actionService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private EntityManager entityManager;

    @MockBean
    private HearingPartService hearingPartService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RulesService rulesService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private FactsMapper factsMapper;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private HearingTypeRepository hearingTypeRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private CaseTypeRepository caseTypeRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private HearingMapper hearingMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void fetchAllHeartingParts_returnHearingPartsFromService() throws Exception {
        val hearingPartResponses = crateHearingPartResponse();
        when(hearingPartService.getAllHearingParts()).thenReturn(hearingPartResponses);

        val response = mvc.getAndMapResponse(URL, new TypeReference<List<HearingPartResponse>>(){});
        assertEquals(response.size(), 1);
        assertThat(response.get(0)).isEqualToComparingFieldByFieldRecursively(hearingPartResponses.get(0));
    }

    @Test
    public void fetchAllHeartingParts_whenPassIsListedAsFalse_returnUnlistedHearingPartsFromService() throws Exception {
        val hearingPartResponses = crateHearingPartResponse();
        when(hearingPartService.getAllHearingPartsThat(any())).thenReturn(hearingPartResponses);

        val response = mvc.getAndMapResponse(URL_IS_LISTED_FALSE, new TypeReference<List<HearingPartResponse>>(){});
        assertEquals(response.size(), 1);
        assertThat(response.get(0)).isEqualToComparingFieldByFieldRecursively(hearingPartResponses.get(0));
    }

    @Test
    public void createHearingPartAction_createsHearingPartAction() throws Exception {
        val ut = createUserTransaction();
        when(actionService.execute(any())).thenReturn(ut);

        val hearingPart = createCreateHearingPart();
        val response = mvc.callAndMapResponse(
            put(URL + "/create"), objectMapper.writeValueAsString(hearingPart), UserTransaction.class
        );

        assertThat(response).isEqualTo(ut);
    }

    @Test
    public void updateHearingPartAction_updateHearingPartAction() throws Exception {
        val ut = createUserTransaction();
        when(actionService.execute(any())).thenReturn(ut);

        val updateBody = createUpdateListingRequest();
        val response = mvc.callAndMapResponse(
            put(URL + "/update"), objectMapper.writeValueAsString(updateBody), UserTransaction.class
        );

        assertThat(response).isEqualTo(ut);
    }

    @Test
    public void deleteHearingPartAction_deletesHearingPart() throws Exception {
        val ut = createUserTransaction();
        when(actionService.execute(any())).thenReturn(ut);

        val response = mvc.callAndMapResponse(
            post(URL + "/delete"), createDeleteListingRequest(), UserTransaction.class
        );

        assertThat(response).isEqualTo(ut);
    }

    @Test
    public void getHearingPartResponse_shouldReturnValidResponse() throws Exception {
        when(hearingPartRepository.findOne(createUuid())).thenReturn(createHearingPart());

        val response = mvc.getAndMapResponse(URL + "/" + createUuid(),
            HearingPartResponse.class);

        assertThat(response).isEqualTo(createHearingPartResponse(createUuid()));
    }

    @Test
    public void assignHearingPartToSession_shouldAssignProperly() throws Exception {
        val ut = createUserTransaction();

        when(hearingPartService.assignHearingPartToSessionWithTransaction(createUuid(), createAssignment()))
            .thenReturn(ut);

        val response = mvc.callAndMapResponse(put(URL + "/" + createUuid()), createAssignment(),
            UserTransaction.class);

        assertThat(response).isEqualTo(ut);
    }

    private HearingPartSessionRelationship createAssignment() {
        val assignment = new HearingPartSessionRelationship();

        return assignment;
    }

    private HearingPartResponse createHearingPartResponse(UUID hpId) {
        val hp = createHearingPart();
        hp.setId(hpId);

        return new HearingPartResponse(hp);
    }

    private List<HearingPartResponse> crateHearingPartResponse() {
        return Arrays.asList(createHearingPart())
            .stream()
            .map(hp -> new HearingPartResponse(hp))
            .collect(Collectors.toList());
    }

    private HearingPart createHearingPart() {
        val hearingPart = new HearingPart();
        hearingPart.setId(createUuid());
        hearingPart.setHearing(createHearing());

        return hearingPart;
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setCaseType(new CaseType());
        hearing.setHearingType(new HearingType());

        return hearing;
    }

    private UserTransaction createUserTransaction() {
        var ut = new UserTransaction();
        ut.setId(UUID.randomUUID());

        return ut;
    }

    private DeleteListingRequest createDeleteListingRequest() {
        return new DeleteListingRequest();
    }

    private CreateHearingRequest createCreateHearingPart() {
        val chp = new CreateHearingRequest();
        chp.setId(createUuid());
        chp.setDuration(createDuration());
        chp.setScheduleStart(createOffsetDateTime());
        chp.setScheduleEnd(createOffsetDateTime());
        chp.setCaseTypeCode(CASE_TYPE_CODE);
        chp.setCaseNumber(CASE_NUMBER);
        chp.setCaseTitle(TITLE);
        chp.setHearingTypeCode(HEARING_TYPE_CODE);
        chp.setDuration(createDuration());
        chp.setPriority(Priority.Low);
        chp.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        chp.setReservedJudgeId(RESERVED_JUDGE_ID);
        chp.setUserTransactionId(UUID.randomUUID());

        return chp;
    }

    private UpdateListingRequest createUpdateListingRequest() {
        val ulr = new UpdateListingRequest();
        ulr.setId(createUuid());
        ulr.setDuration(createDuration());
        ulr.setScheduleStart(createOffsetDateTime());
        ulr.setScheduleEnd(createOffsetDateTime());
        ulr.setCaseTypeCode(CASE_TYPE_CODE);
        ulr.setCaseNumber(CASE_NUMBER);
        ulr.setCaseTitle(TITLE);
        ulr.setHearingTypeCode(HEARING_TYPE_CODE);
        ulr.setDuration(createDuration());
        ulr.setPriority(Priority.Low);
        ulr.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        ulr.setReservedJudgeId(RESERVED_JUDGE_ID);
        ulr.setUserTransactionId(UUID.randomUUID());
        ulr.setVersion(0L);

        return ulr;
    }

    private OffsetDateTime createOffsetDateTime() {
        return OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);
    }

    private Duration createDuration() {
        return Duration.ofDays(1L);
    }

    private UUID createUuid() {
        return UUID.fromString("ccd97860-4345-405f-b004-f92f90215fff");
    }
}
