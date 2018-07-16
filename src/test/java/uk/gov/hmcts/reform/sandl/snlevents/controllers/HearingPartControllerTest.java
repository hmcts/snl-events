package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingPartController.class)
public class HearingPartControllerTest {

    private static final OffsetDateTime CREATED_AT = OffsetDateTime.MAX;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HearingPartService hearingPartService;

    @MockBean
    private RulesService rulesService;

    @MockBean
    private FactsMapper factsMapper;

    private JacksonTester<List> hearingPartsSerializer;

    private JacksonTester<CreateHearingPart> createHearingPartSerializer;

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        objectMapper = new ObjectMapper();
    }

    @Test
    public void fetchAllHeartingParts_returnHearingPartsFromService() throws Exception {
        val hearingParts = createHearingParts();
        when(hearingPartService.getAllHearingParts()).thenReturn(hearingParts);

        val response = mvc
            .perform(get("/hearing-part"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        assertResponseEquals(hearingPartsSerializer, response, hearingParts);
    }

    private List<HearingPart> createHearingParts() {
        return new ArrayList<>(Arrays.asList(createHearingPart()));
    }

    private HearingPart createHearingPart() {
        return new HearingPart();
    }

    //@Test
    public void upsertHearingPart_savesHearingPartToService() throws Exception {
        val hearingPart = createHearingPart();

        when(hearingPartService.save(any(HearingPart.class))).then(returnsFirstArg());

        val response = mvc
            .perform(put("/hearing-part").contentType(MediaType.APPLICATION_JSON).content(
                createHearingPartSerializer.write(createCreateHearingPart()).getJson()
            )).andExpect(status().isOk()).andReturn().getResponse();

        val r = objectMapper.readValue(response.getContentAsString(), HearingPart.class);
        assertThat(r).isEqualToIgnoringGivenFields(hearingPart, "createdAt");
    }

    private CreateHearingPart createCreateHearingPart() {
        val ch = new CreateHearingPart();
        //todo investigate why setting created_ad ruins everything
        ch.setCreatedAt(CREATED_AT);

        return ch;
    }

    private void assertResponseEquals(
        JacksonTester serializer, MockHttpServletResponse response, Object expected
    ) throws IOException {
        assertThat(response.getContentAsString()).isEqualTo(
            serializer.write(expected).getJson()
        );
    }
}
