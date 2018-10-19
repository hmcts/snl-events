package uk.gov.hmcts.reform.sandl.snlevents.models.response;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingWithSessionResponseTests {

    private static final UUID ID =UUID.randomUUID();
    private static final String CASE_NUMBER = "case-number";
    private static final String CASE_TITLE = "case-title";
    private static final String HEARING_TYPE_DESCRIPTION = "ht-description";
    private static final String CASE_TYPE_DESCRIPTION = "ct-description";
    private static final Duration DURATION = Duration.ofMinutes(10);
    private static final OffsetDateTime SCHEDULE_START = OffsetDateTime.of(1,1,1,1,1,1,1,ZoneOffset.UTC);
    private static final OffsetDateTime SCHEDULE_END = OffsetDateTime.of(2,2,2,2,2,2,2,ZoneOffset.UTC);
    private static final Priority PRIORITY = Priority.High;
    private static final String COMMUNICATION_FACILITATOR = "communication-facilitator";
    private static final String JUDGE_NAME = "judge-name";

    @Test
    public void hearingWithSessionResponseMapsHearingFromDb()
    {
        val hearing = createHearing();

        val expectedResponse = createExpectedResponse();

        val response = new HearingWithSessionsResponse(hearing);

        assertThat(response).isEqualTo(expectedResponse);
    }

    private Hearing createHearing() {
        val hearing = new Hearing();
        hearing.setId(ID);
        hearing.setCaseNumber(CASE_NUMBER);
        hearing.setCaseTitle(CASE_TITLE);
        hearing.setCaseType(createCaseType());
        hearing.setHearingType(createHearingType());
        hearing.setDuration(DURATION);
        hearing.setScheduleStart(SCHEDULE_START);
        hearing.setScheduleEnd(SCHEDULE_END);
        hearing.setPriority(PRIORITY);
        hearing.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
        hearing.setReservedJudge(createPerson());

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
