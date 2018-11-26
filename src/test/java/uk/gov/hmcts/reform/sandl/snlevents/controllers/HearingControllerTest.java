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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.StatusesTestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.ActionService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingPartService;
import uk.gov.hmcts.reform.sandl.snlevents.service.HearingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@RunWith(SpringRunner.class)
@WebMvcTest(HearingController.class)
@Import( {TestConfiguration.class, StatusesTestConfiguration.class})
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
    private SessionRepository sessionRepository;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ActionService actionService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void getHearingsForListing_shouldReturnProperHearings() throws Exception {
        val uuid = ID.randomUUID();
        val hearing = createHearing();
        hearing.setId(uuid);
        when(hearingService.getHearingsForListing(Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty())).thenReturn(new PageImpl(Arrays.asList(HearingForListingResponse.builder().build())));

        mvc.getResponseAsString(URL + "/for-listing");

        verify(hearingService, times(1)).getHearingsForListing(Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
    }

    @Test
    public void getHearingByIdForAmendment_shouldReturnProperHearing() throws Exception {
        val uuid = ID.randomUUID();
        val hearing = createHearingForAmend();
        hearing.setId(uuid);
        when(hearingService.get(uuid)).thenReturn(hearing);

        val response = mvc.getAndMapResponse(URL + "/" + uuid + "/for-amendment",
            new TypeReference<HearingSearchResponseForAmendment>() {
            });
        assertThat(response.getId()).isEqualTo(hearing.getId());
    }

    @Test
    public void assignHearingToSession_shouldReturnUserTransaction() throws Exception {
        val ut = createUserTransaction();

        when(actionService.execute(any())).thenReturn(ut);

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
        expectedResponse.setStatus(Status.Listed);

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

        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Listed);
        h.setStatus(statusConfig);

        return h;
    }

    private HearingSearchResponseForAmendment createHearingForAmend() {
        val hearing = new HearingSearchResponseForAmendment();

        return hearing;
    }
}
