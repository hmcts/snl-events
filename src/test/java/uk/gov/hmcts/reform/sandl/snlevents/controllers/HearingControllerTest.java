package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingService;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class HearingControllerTest {
    public static final String URL = "/hearing";
    private static final UUID ID = java.util.UUID.fromString("f9a3867b-0d15-419d-bd98-40d247139131");

    @Autowired
    private EventsMockMvc mvc;

    @MockBean
    private HearingPartService hearingPartService;

    @MockBean
    private HearingService hearingService;

    @MockBean
    private HearingRepository hearingRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Test
    public void getHearingById_shouldReturnProperHearing() throws Exception {
        val uuid = ID.randomUUID();
        val hearing = createHearing();
        hearing.setId(uuid);
        when(hearingRepository.findOne(uuid)).thenReturn(hearing);

        val response = mvc.getAndMapResponse(URL + "/" + uuid, new TypeReference<HearingInfo>() {
        });
        assertThat(response.getId()).isEqualTo(hearing.getId());
    }

    @Test
    public void getHearingByIdForAmendment_shouldReturnProperHearing() throws Exception {
        val uuid = ID.randomUUID();
        val hearing = createHearingForAmend();
        hearing.setId(uuid);
        when(hearingService.get(uuid)).thenReturn(hearing);

        val response = mvc.getAndMapResponse(URL + "/" + uuid + "/for-amendment",
            new TypeReference<HearingSearchResponseForAmendment>() {});
        assertThat(response.getId()).isEqualTo(hearing.getId());
    }

    @Test
    public void assignHearingToSession_shouldReturnUserTransaction() throws Exception {
        val ut = createUserTransaction();

        when(hearingPartService.assignHearingToSessionWithTransaction(ID, createAssignment()))
            .thenReturn(ut);

        val response = mvc.callAndMapResponse(put(URL + "/" + ID), createAssignment(),
            UserTransaction.class);

        assertThat(response).isEqualTo(ut);
    }

    @Test
    public void getHearingByIdWithSessionsReturnsHearingWithSessions() throws Exception {
        val hearing = createHearing();
        hearing.setId(ID);

        when(hearingRepository.findOne(ID)).thenReturn(hearing);

        val expectedResponse = new HearingWithSessionsResponse();
        expectedResponse.setId(ID);
        expectedResponse.setPriority(Priority.High.toString());
        expectedResponse.setCaseType("desc");
        expectedResponse.setHearingType("desc");
        expectedResponse.setSessions(Collections.emptyList());
        expectedResponse.setHearingPartsVersions(Collections.emptyList());

        val response = mvc.getAndMapResponse(URL + "/" + ID + "/with-sessions", HearingWithSessionsResponse.class);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    public void unlistHearing_shouldReturnUserTransaction() throws Exception {
        val ut = createUserTransaction();

        when(hearingService.unlist(any())).thenReturn(ut);

        val response = mvc.callAndMapResponse(put(URL + "/unlist"), new UnlistHearingRequest(),
            UserTransaction.class);

        assertThat(response).isEqualTo(ut);
    }

    private UserTransaction createUserTransaction() {
        var ut = new UserTransaction();
        ut.setId(ID.randomUUID());

        return ut;
    }

    private HearingSessionRelationship createAssignment() {
        val assignment = new HearingSessionRelationship();

        return assignment;
    }

    private Hearing createHearing() {
        Hearing h = new Hearing();
        h.setHearingType(new HearingType("code", "desc"));
        h.setCaseType(new CaseType("code", "desc"));
        h.setPriority(Priority.High);
        return h;
    }

    private HearingSearchResponseForAmendment createHearingForAmend() {
        val hearing = new HearingSearchResponseForAmendment();
        hearing.setListedCount(1L);

        return hearing;
    }
}
