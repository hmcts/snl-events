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
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.RoomResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class HearingControllerTest {
    public static final String URL = "/hearing";

    @Autowired
    private EventsMockMvc mvc;

    @MockBean
    private HearingPartService hearingPartService;
    @MockBean
    private HearingRepository hearingRepository;
    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Test
    public void fetchAllRooms_returnsRoomsFromService() throws Exception {
        val uuid = UUID.randomUUID();
        val hearing = createHearing();
        hearing.setId(uuid);
        when(hearingRepository.findOne(uuid)).thenReturn(hearing);

        val response = mvc.getAndMapResponse(URL + "/" + uuid, new TypeReference<HearingInfo>() {});
        assertThat(response.getId()).isEqualTo(hearing.getId());
    }

    private Hearing createHearing() {
        Hearing h = new Hearing();
        h.setHearingType(new HearingType("code", "desc"));
        h.setCaseType(new CaseType("code", "desc"));
        return h;
    }
}
