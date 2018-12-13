package uk.gov.hmcts.reform.sandl.snlevents.models.response;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingWithSessionsResponseTests {

    private static final UUID ID = UUID.randomUUID();
    private static final String CASE_NUMBER = "case-number";
    private static final String CASE_TITLE = "case-title";
    private static final String HEARING_TYPE_DESCRIPTION = "ht-description";
    private static final String CASE_TYPE_DESCRIPTION = "ct-description";
    private static final Duration DURATION = Duration.ofMinutes(10);
    private static final OffsetDateTime SCHEDULE_START = OffsetDateTime.of(1, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC);
    private static final OffsetDateTime SCHEDULE_END = OffsetDateTime.of(2, 2, 2, 2, 2, 2, 2, ZoneOffset.UTC);
    private static final Priority PRIORITY = Priority.High;
    private static final String COMMUNICATION_FACILITATOR = "communication-facilitator";
    private static final String JUDGE_NAME = "judge-name";
    private static final Long VERSION = 0L;

    @Test
    public void hearingWithSessionsResponseConstructor_mapsHearingFromDb() {
        val hearing = createHearing();

        val expectedResponse = createExpectedResponse();

        val response = new HearingWithSessionsResponse(hearing);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    public void hearingWithSessionsResponseConstructor_sortsSessionsByStartAscending() {
        val hearing = createMinimalHearing();

        hearing.setHearingParts(
            Arrays.asList(
                createHearingPartWithSession(1, hearing),
                createHearingPartWithSession(3, hearing),
                createHearingPartWithSession(2, hearing)
            )
        );

        val response = new HearingWithSessionsResponse(hearing);

        // assert correct order of sessions
        assertThat(response.getSessions().get(0).getHearingPartStartTime().getDayOfMonth()).isEqualTo(1);
        assertThat(response.getSessions().get(1).getHearingPartStartTime().getDayOfMonth()).isEqualTo(2);
        assertThat(response.getSessions().get(2).getHearingPartStartTime().getDayOfMonth()).isEqualTo(3);
    }

    private HearingPart createHearingPartWithSession(int day, Hearing hearing) {
        val sessionType = new SessionType();
        sessionType.setDescription("desc");

        val session = new Session();
        session.setSessionType(sessionType);
        session.setStart(OffsetDateTime.of(2000, 1, day, 0, 0, 0, 0, ZoneOffset.UTC));

        val hearingPart = new HearingPart();
        hearingPart.setSession(session);
        hearingPart.setStatus(new StatusesMock().statusConfigService.getStatusConfig(Status.Listed));
        hearingPart.setHearing(hearing);
        hearingPart.setStart(OffsetDateTime.of(2000, 1, day, 0, 0, 0, 0, ZoneOffset.UTC));
        hearing.setHearingParts(Arrays.asList(hearingPart));

        session.setHearingParts(Arrays.asList(hearingPart));

        return hearingPart;
    }

    private Hearing createMinimalHearing() {
        val hearing = new Hearing();
        hearing.setCaseType(createCaseType());
        hearing.setHearingType(createHearingType());
        hearing.setPriority(PRIORITY);
        hearing.setVersion(VERSION);
        hearing.setId(UUID.randomUUID());

        val status = new StatusConfig();
        status.setStatus(Status.Listed);
        hearing.setStatus(status);

        return hearing;
    }

    private Hearing createHearing() {
        val hearing = createMinimalHearing();
        hearing.setId(ID);
        hearing.setCaseNumber(CASE_NUMBER);
        hearing.setCaseTitle(CASE_TITLE);
        hearing.setDuration(DURATION);
        hearing.setScheduleStart(SCHEDULE_START);
        hearing.setScheduleEnd(SCHEDULE_END);
        hearing.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        hearing.setReservedJudge(createPerson());
        hearing.setVersion(VERSION);
        val status = new StatusConfig();
        status.setStatus(Status.Listed);
        hearing.setStatus(status);

        return hearing;
    }

    private HearingWithSessionsResponse createExpectedResponse() {
        val response = new HearingWithSessionsResponse();
        response.setId(ID);
        response.setCaseNumber(CASE_NUMBER);
        response.setCaseTitle(CASE_TITLE);
        response.setCaseType(CASE_TYPE_DESCRIPTION);
        response.setHearingType(HEARING_TYPE_DESCRIPTION);
        response.setDuration(DURATION);
        response.setScheduleStart(SCHEDULE_START);
        response.setScheduleEnd(SCHEDULE_END);
        response.setPriority(Priority.High.toString());
        response.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        response.setReservedToJudge(JUDGE_NAME);
        response.setSessions(Collections.emptyList());
        response.setHearingPartsVersions(new ArrayList<>());
        response.setStatus(Status.Listed);
        val status = new StatusConfig();
        status.setStatus(Status.Listed);
        response.setStatusConfig(status);

        return response;
    }

    private CaseType createCaseType() {
        val caseType = new CaseType();
        caseType.setDescription(CASE_TYPE_DESCRIPTION);

        return caseType;
    }

    private HearingType createHearingType() {
        val hearingType = new HearingType();
        hearingType.setDescription(HEARING_TYPE_DESCRIPTION);

        return hearingType;
    }

    private Person createPerson() {
        val person = new Person();
        person.setName(JUDGE_NAME);

        return person;
    }
}
