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
import uk.gov.hmcts.reform.sandl.snlevents.mappers.HearingPartMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DeleteListingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingPartResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
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
    private HearingPartMapper hearingPartMapper;

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
    public void upsertHearingPart_savesHearingPartToService() throws Exception {
        HearingPartResponse hearingPartResponse = crateHearingPartResponse().get(0);
        when(hearingPartService.createHearingPart(any(CreateHearingPartRequest.class))).thenReturn(hearingPartResponse);
        when(hearingTypeRepository.findOne(any(String.class))).thenReturn(HEARING_TYPE);
        val content = objectMapper.writeValueAsString(createCreateHearingPart());

        val response = mvc.callAndMapResponse(put(URL), content, HearingPartResponse.class);
        assertThat(response).isEqualToComparingFieldByFieldRecursively(hearingPartResponse);
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
    public void deleteHearingPartAction_deletesHearingPart() throws Exception {
        val ut = createUserTransaction();
        when(actionService.execute(any())).thenReturn(ut);

        val response = mvc.callAndMapResponse(
            post(URL + "/delete"), createDeleteListingRequest(), UserTransaction.class
        );

        assertThat(response).isEqualTo(ut);
    }

    private List<HearingPartResponse> crateHearingPartResponse() {
        return Arrays.asList(createHearingPart())
            .stream()
            .map(hp -> new HearingPartResponse(hp))
            .collect(Collectors.toList());
    }

    private UserTransaction createUserTransaction() {
        var ut = new UserTransaction();
        ut.setId(UUID.randomUUID());

        return ut;
    }

    private DeleteListingRequest createDeleteListingRequest() {
        return new DeleteListingRequest();
    }

    private CreateHearingPartRequest createCreateHearingPart() {
        val chp = new CreateHearingPartRequest();
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

    private OffsetDateTime createOffsetDateTime() {
        return OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);
    }

    private Duration createDuration() {
        return Duration.ofDays(1L);
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setId(createUuid());
        hp.setDuration(createDuration());
        hp.setScheduleStart(createOffsetDateTime());
        hp.setScheduleEnd(createOffsetDateTime());
        hp.setCaseType(CASE_TYPE);
        hp.setCaseNumber(CASE_NUMBER);
        hp.setCaseTitle(TITLE);
        hp.setHearingType(HEARING_TYPE);
        hp.setDuration(createDuration());
        hp.setPriority(Priority.Low);
        hp.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        hp.setReservedJudgeId(RESERVED_JUDGE_ID);

        return hp;
    }

    private UUID createUuid() {
        return UUID.fromString("ccd97860-4345-405f-b004-f92f90215fff");
    }
}
