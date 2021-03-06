package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class FactsMapperTest {

    private static final int DURATION = 1;
    private static final String DURATION_MAPPED = "86400";

    private static final OffsetDateTime START = OffsetDateTime.MIN;
    private static final String START_MAPPED = "-999999999-01-01T00:00:00+18:00";
    private static final OffsetDateTime END = OffsetDateTime.MAX;
    private static final String END_MAPPED = "+999999999-12-31T23:59:59.999999999-18:00";

    private static final String CASE_TYPE_CODE = "case-type";
    private static final String CASE_TYPE_DESC = "case-type-desc";
    private static final CaseType CASE_TYPE = new CaseType(CASE_TYPE_CODE, CASE_TYPE_DESC);
    private static final String CASE_TYPE_2 = "case-type-2";
    private static final String CASE_TYPE_DESC_2 = "case-type-desc-2";
    private static final String SESSION_TYPE = "session-type";
    private static final String SESSION_TYPE_DESC = "session-type-desc";
    private static final String HEARING_TYPE_DESC = "hearing-type-desc";
    private static final String HEARING_TYPE_CODE = "hearing-type-2";
    private static final HearingType HEARING_TYPE = new HearingType(HEARING_TYPE_CODE, HEARING_TYPE_DESC);
    private static final String HEARING_TYPE_CODE_2 = "hearing-type-2";
    private static final String HEARING_TYPE_DESC_2 = "hearing-type-desc-2";
    private static final HearingType HEARING_TYPE_2 = new HearingType(HEARING_TYPE_CODE_2, HEARING_TYPE_DESC_2);
    private static final String ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String SESSION_ID = "83537f81-b730-4754-aba4-0277b7882824";
    private static final String PERSON_ID = "1fa92e14-ce0e-4a1f-b352-53f1581d771f";
    private static final String ROOM_ID = "3a8b6e05-afb9-4b2a-b87d-152971d0607a";
    private static final String HP_ID = "d26119ab-f3fa-47c9-9733-047b744a0c8a";

    public static final int VALUE = 123;
    private static final String PERSON_NAME = "Grzesiek";
    private static final String ROOM_NAME = "Living room";


    FactsMapper factsMapper = new FactsMapper();

    @Test
    public void mapCreateSessionToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapCreateSessionToRuleJsonMessage(createUpsertSession());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"sessionType\":\"" + SESSION_TYPE + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapUpdateSessionToRuleJsonMessage_mapsOk() {
        val sessionWithHearingPartsFacts = factsMapper
            .mapUpdateSessionToRuleJsonMessage(createSession(), Collections.emptyList());

        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"sessionType\":\"" + SESSION_TYPE + "\""
            + "}";

        assertThat(sessionWithHearingPartsFacts.getSessionFact()).isEqualTo(expected);
        assertTrue(sessionWithHearingPartsFacts.getHearingPartsFacts().isEmpty());
    }

    @Test
    public void mapUpdateSessionToRuleJsonMessage_SessionWithHearingPart_MapsSessionAndHearingsParts() {
        val session = createHearingPart().getSession();
        val sessionWithHearingPartsFacts = factsMapper
            .mapUpdateSessionToRuleJsonMessage(session, session.getHearingParts());

        val expectedSessionFact = "{"
            + "\"id\":\"" + SESSION_ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"sessionType\":\"" + SESSION_TYPE + "\""
            + "}";

        val expectedHearingPartFact = "{\"id\":\"d26119ab-f3fa-47c9-9733-047b744a0c8a\","
            + "\"sessionId\":\"" + SESSION_ID + "\","
            + "\"caseTypeCode\":\"case-type\","
            + "\"hearingTypeCode\":\"hearing-type-2\","
            + "\"duration\":86400,"
            + "\"scheduleStart\":\"-999999999-01-01T00:00:00+18:00\","
            + "\"scheduleEnd\":\"+999999999-12-31T23:59:59.999999999-18:00\","
            + "\"createdAt\":\"-999999999-01-01T00:00:00+18:00\"}";

        assertThat(sessionWithHearingPartsFacts.getSessionFact()).isEqualTo(expectedSessionFact);
        assertThat(sessionWithHearingPartsFacts.getHearingPartsFacts().size()).isEqualTo(1);
        assertThat(sessionWithHearingPartsFacts.getHearingPartsFacts().get(0)).isEqualTo(expectedHearingPartFact);
    }

    @Test
    public void mapDbSessionToRuleJsonMessage_mapsOk() {
        val mapped = factsMapper.mapDbSessionToRuleJsonMessage(createSession(), Collections.emptyList())
            .getSessionFact();
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"sessionType\":\"" + SESSION_TYPE + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);

    }

    @Test
    public void mapDbHearingToRuleJsonMessage_mapsOk() {
        val mapped = factsMapper.mapHearingToRuleJsonMessage(createHearing().getHearingParts().get(0));
        val expected = "{"
            + "\"id\":\"" + HP_ID + "\","
            + "\"sessionId\":\"" + SESSION_ID + "\","
            + "\"caseTypeCode\":\"" + CASE_TYPE_CODE + "\","
            + "\"hearingTypeCode\":\"" + HEARING_TYPE_CODE + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"scheduleStart\":\"" + START_MAPPED + "\","
            + "\"scheduleEnd\":\"" + END_MAPPED + "\","
            + "\"createdAt\":\"" + START_MAPPED + "\""
            + "}";
        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapDbSessionTypeToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapDbSessionTypeToRuleJsonMessage(createSessionType());

        val expected = "{\"id\":\"" + SESSION_TYPE + "\","
            + "\"caseTypes\":"
            + "[{\"code\":\"" + CASE_TYPE_2 + "\",\"description\":\"" + CASE_TYPE_DESC_2 + "\"},"
            + "{\"code\":\"" + CASE_TYPE_CODE + "\",\"description\":\"" + CASE_TYPE_DESC + "\"}],"
            + "\"hearingTypes\":"
            + "[{\"code\":\"" + HEARING_TYPE_CODE + "\",\"description\":\"" + HEARING_TYPE_DESC + "\"},"
            + "{\"code\":\"" + HEARING_TYPE_CODE_2 + "\",\"description\":\"" + HEARING_TYPE_DESC_2 + "\"}]}";
        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapDbRoomToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapDbRoomToRuleJsonMessage(createRoom());
        val expected = "{\"id\":\"" + ROOM_ID + "\",\"name\":\"" + ROOM_NAME + "\"}";

        assertThat(mapped).isEqualTo(expected);
    }

    private Room createRoom() {
        val r = new Room();
        r.setId(createUuid(ROOM_ID));
        r.setName(ROOM_NAME);

        return r;
    }

    @Test
    public void mapDbPersonToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapDbPersonToRuleJsonMessage(createPerson());
        val expected = "{"
            + "\"id\":\"" + PERSON_ID + "\","
            + "\"name\":\"" + PERSON_NAME + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    private Person createPerson() {
        val p = new Person();
        p.setId(createUuid(PERSON_ID));
        p.setName(PERSON_NAME);

        return p;
    }

    @Test
    public void mapHearingPartToRuleJsonMessage_mapsOk() {
        val mapped = factsMapper.mapHearingPartToRuleJsonMessage(createHearingPart());
        val expected = "{\"id\":\"d26119ab-f3fa-47c9-9733-047b744a0c8a\","
            + "\"sessionId\":\"" + SESSION_ID + "\","
            + "\"caseTypeCode\":\"case-type\","
            + "\"hearingTypeCode\":\"hearing-type-2\","
            + "\"duration\":86400,"
            + "\"scheduleStart\":\"-999999999-01-01T00:00:00+18:00\","
            + "\"scheduleEnd\":\"+999999999-12-31T23:59:59.999999999-18:00\","
            + "\"createdAt\":\"-999999999-01-01T00:00:00+18:00\"}";

        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapHearingPartToRuleJsonMessage_WhenHearingIsMultiSession_ShouldSetSessionDurationAsHPartDuration() {
        val hearingPart = createHearingPart();
        val session = hearingPart.getSession();
        val hearing = hearingPart.getHearing();
        hearing.setMultiSession(true);

        val expectedHearingPartFact = "{\"id\":\"d26119ab-f3fa-47c9-9733-047b744a0c8a\","
            + "\"sessionId\":\"" + SESSION_ID + "\","
            + "\"caseTypeCode\":\"case-type\","
            + "\"hearingTypeCode\":\"hearing-type-2\","
            + "\"duration\":" + session.getDuration().getSeconds() + ","
            + "\"scheduleStart\":\"-999999999-01-01T00:00:00+18:00\","
            + "\"scheduleEnd\":\"+999999999-12-31T23:59:59.999999999-18:00\","
            + "\"createdAt\":\"-999999999-01-01T00:00:00+18:00\"}";

        val hearingPartFact = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);

        assertThat(hearingPartFact).isEqualTo(expectedHearingPartFact);
    }

    @Test
    public void mapHearingPartToRuleJsonMessage_WhenHearingIsMultiSession_ShouldSetHearingDurationAsHPartDuration() {
        val hearingPart = createHearingPart();
        val hearing = hearingPart.getHearing();
        hearing.setMultiSession(true);

        val expectedHearingPartFact = "{\"id\":\"d26119ab-f3fa-47c9-9733-047b744a0c8a\","
            + "\"sessionId\":\"" + SESSION_ID + "\","
            + "\"caseTypeCode\":\"case-type\","
            + "\"hearingTypeCode\":\"hearing-type-2\","
            + "\"duration\":" + hearing.getDuration().getSeconds() + ","
            + "\"scheduleStart\":\"-999999999-01-01T00:00:00+18:00\","
            + "\"scheduleEnd\":\"+999999999-12-31T23:59:59.999999999-18:00\","
            + "\"createdAt\":\"-999999999-01-01T00:00:00+18:00\"}";

        val hearingPartFact = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);

        assertThat(hearingPartFact).isEqualTo(expectedHearingPartFact);
    }


    @Test
    public void mapTimeToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapTimeToRuleJsonMessage(createDateTimePartValue());
        val expected = "{"
            + "\"id\":null,"
            + "\"value\":" + VALUE
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    private DateTimePartValue createDateTimePartValue() {
        val dtpv = new DateTimePartValue();
        dtpv.setValue(VALUE);

        return dtpv;
    }


    private UpsertSession createUpsertSession() {
        val us = new UpsertSession();
        us.setId(createUuid());
        us.setDuration(createDuration());
        us.setStart(START);
        us.setSessionTypeCode(SESSION_TYPE);
        us.setRoomId(createUuid(ROOM_ID).toString());
        us.setPersonId(createUuid(PERSON_ID).toString());

        return us;
    }

    private Session createSession() {
        val s = new Session();
        s.setId(createUuid());
        s.setDuration(createDuration());
        s.setStart(START);
        s.setSessionType(new SessionType(SESSION_TYPE, SESSION_TYPE_DESC));
        s.setPerson(createPerson());
        s.setRoom(createRoom());

        return s;
    }

    private SessionType createSessionType() {
        val st = new SessionType();
        st.setCode(SESSION_TYPE);
        st.setDescription(SESSION_TYPE_DESC);
        st.addHearingType(HEARING_TYPE);
        st.addHearingType(HEARING_TYPE_2);
        st.addCaseType(new CaseType(CASE_TYPE_CODE, CASE_TYPE_DESC));
        st.addCaseType(new CaseType(CASE_TYPE_2, CASE_TYPE_DESC_2));
        return st;
    }

    private Hearing createHearing() {
        val h = new Hearing();
        h.setId(createUuid());
        h.setDuration(createDuration());
        h.setCaseType(CASE_TYPE);
        h.setHearingType(HEARING_TYPE);
        h.setScheduleStart(START);
        h.setScheduleEnd(END);
        h.setCreatedAt(START);
        val hp = new HearingPart();
        hp.setId(createUuid(HP_ID));
        hp.setHearing(h);
        hp.setSessionId(createUuid(SESSION_ID));
        h.setHearingParts(Arrays.asList(hp));

        return h;
    }

    private HearingPart createHearingPart() {
        val h = new Hearing();
        h.setId(createUuid());
        h.setDuration(createDuration());
        h.setCaseType(CASE_TYPE);
        h.setHearingType(HEARING_TYPE);
        h.setScheduleStart(START);
        h.setScheduleEnd(END);
        h.setCreatedAt(START);

        val session = createSession();
        session.setId(createUuid(SESSION_ID));

        val hp = new HearingPart();
        hp.setId(createUuid(HP_ID));
        hp.setSession(session);
        h.setHearingParts(Arrays.asList(hp));
        hp.setHearing(h);

        session.addHearingPart(hp);

        return hp;
    }

    private UUID createUuid() {
        return createUuid(ID);
    }

    private UUID createUuid(String uuid) {
        return UUID.fromString(uuid);
    }

    private Duration createDuration() {
        return Duration.ofDays(DURATION);
    }
}
