package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingPartController.class)
@Import(TestConfiguration.class)
public class HearingPartControllerTest {
    public static final String TYPE = "type";
    public static final String CASE_NUMBER = "90";
    public static final String TITLE = "title";
    public static final String HEARING_TYPE = "hearing-type";
    public static final String URL = "/hearing-part";

    @MockBean
    private HearingPartService hearingPartService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private RulesService rulesService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SAuthenticationService s2SAuthenticationService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private FactsMapper factsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void fetchAllHeartingParts_returnHearingPartsFromService() throws Exception {
        val hearingParts = createHearingParts();
        when(hearingPartService.getAllHearingParts()).thenReturn(hearingParts);

        val response = mvc.getAndMapResponse(URL, new TypeReference<List<HearingPart>>(){});
        assertEquals(response.size(), 1);
        assertThat(response.get(0)).isEqualToComparingFieldByFieldRecursively(hearingParts.get(0));
    }

    private List<HearingPart> createHearingParts() {
        return Arrays.asList(createHearingPart());
    }

    @Test
    public void upsertHearingPart_savesHearingPartToService() throws Exception {
        when(hearingPartService.save(any(HearingPart.class))).then(returnsFirstArg());
        val content = objectMapper.writeValueAsString(createCreateHearingPart());

        val response = mvc.putAndMapResponse(URL, content, HearingPart.class);
        assertThat(response).isEqualToComparingFieldByFieldRecursively(createHearingPart());
    }

    private CreateHearingPart createCreateHearingPart() {
        val chp = new CreateHearingPart();
        chp.setId(createUuid());
        chp.setDuration(createDuration());
        chp.setScheduleStart(createOffsetDateTime());
        chp.setScheduleEnd(createOffsetDateTime());
        chp.setCaseType(TYPE);
        chp.setCaseNumber(CASE_NUMBER);
        chp.setCaseTitle(TITLE);
        chp.setHearingType(HEARING_TYPE);
        chp.setDuration(createDuration());
        chp.setCreatedAt(createOffsetDateTime());

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
        hp.setCaseType(TYPE);
        hp.setCaseNumber(CASE_NUMBER);
        hp.setCaseTitle(TITLE);
        hp.setHearingType(HEARING_TYPE);
        hp.setDuration(createDuration());
        hp.setCreatedAt(createOffsetDateTime());

        return hp;
    }

    private UUID createUuid() {
        return UUID.fromString("ccd97860-4345-405f-b004-f92f90215fff");
    }
}
