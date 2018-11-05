package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.Utils;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.interfaces.SimpleDictionarySettable;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.BaseReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.CaseTypeWithHearingTypesResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReferenceDataService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ReferenceDataController.class, secure = false)
@Import(TestConfiguration.class)
public class ReferenceDataControllerTest {
    public static final String URL_ROOM_TYPES = "/reference/room-types";
    public static final String URL_SESSION_TYPES = "/reference/session-types";
    public static final String URL_HEARING_TYPES = "/reference/hearing-types";
    public static final String URL_CASE_TYPES = "/reference/case-types";

    private static final String DEFAULT_CODE = "sample-code";
    private static final String DEFAULT_DESCRIPTION = "sample description";

    @MockBean
    private ReferenceDataService testService;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Autowired
    private EventsMockMvc mvc;

    @Test
    public void fetchAllRoomTypes_returnsRoomTypesFromService() throws Exception {
        val mockedValues = createRoomTypes();
        when(testService.getRoomTypes()).thenReturn(mockedValues);

        val response = mvc.getAndMapResponse(URL_ROOM_TYPES, new TypeReference<List<SimpleDictionaryData>>() {
        });
        assertThat(response).isEqualTo(mockedValues);
    }

    @Test
    public void fetchAllSessionTypes_returnsSessionTypesFromService() throws Exception {
        val mockedValues = createSessionTypes();
        when(testService.getSessionTypes()).thenReturn(mockedValues);

        val response = mvc.getAndMapResponse(URL_SESSION_TYPES, new TypeReference<List<SimpleDictionaryData>>() {
        });
        assertThat(response).isEqualTo(mockedValues);
    }

    @Test
    public void fetchAllHearingTypes_returnsHearingTypesFromService() throws Exception {
        val mockedValues = createHearingTypes();
        when(testService.getHearingTypes()).thenReturn(mockedValues);

        val response = mvc.getAndMapResponse(URL_HEARING_TYPES, new TypeReference<List<SimpleDictionaryData>>() {
        });
        assertThat(response).isEqualTo(mockedValues);
    }

    @Test
    public void fetchAllCaseTypes_returnsCaseTypesFromService() throws Exception {
        val mockedValues = createCaseTypesResponse();
        when(testService.getCaseTypes()).thenReturn(mockedValues);

        val response = mvc.getAndMapResponse(
            URL_CASE_TYPES, new TypeReference<List<CaseTypeWithHearingTypesResponse>>() {
            });
        assertThat(response).isEqualTo(mockedValues);
    }

    private List<SimpleDictionarySettable> createRoomTypes() {
        return getAsSimpleDictionaryData(Stream.of(new RoomType(DEFAULT_CODE, DEFAULT_DESCRIPTION)));
    }

    private List<SimpleDictionarySettable> createSessionTypes() {
        return getAsSimpleDictionaryData(Stream.of(new SessionType(DEFAULT_CODE, DEFAULT_DESCRIPTION)));
    }

    private List<SimpleDictionarySettable> createHearingTypes() {
        return getAsSimpleDictionaryData(Stream.of(new HearingType(DEFAULT_CODE, DEFAULT_DESCRIPTION)));
    }

    private List<SimpleDictionarySettable> getAsSimpleDictionaryData(Stream<? extends BaseReferenceData> values) {
        return values
            .map(val -> new SimpleDictionaryData(val.getCode(), val.getDescription()))
            .collect(Collectors.toList());
    }

    private List<CaseTypeWithHearingTypesResponse> createCaseTypesResponse() {
        return Utils.getCaseTypeWithHearingTypesResponses(DEFAULT_CODE, DEFAULT_DESCRIPTION);
    }
}
