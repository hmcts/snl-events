package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Availability;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class FactsMapperTest {

    private static final int DURATION = 1;
    private static final String DURATION_MAPPED = "86400";

    private static final OffsetDateTime START = OffsetDateTime.MIN;
    private static final String START_MAPPED = "-999999999-01-01T00:00:00+18:00";
    private static final OffsetDateTime END = OffsetDateTime.MAX;
    private static final String END_MAPPED = "+999999999-12-31T23:59:59.999999999-18:00";

    private static final String CASE_TYPE = "case-type";
    private static final String ID = "123e4567-e89b-12d3-a456-426655440000";
    private static final String PERSON_ID = "1fa92e14-ce0e-4a1f-b352-53f1581d771f";
    private static final String ROOM_ID = "3a8b6e05-afb9-4b2a-b87d-152971d0607a";

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
            + "\"caseType\":\"" + CASE_TYPE + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapUpdateSessionToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapUpdateSessionToRuleJsonMessage(createSession());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"caseType\":\"" + CASE_TYPE + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    public void mapCreateHearingPartToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapCreateHearingPartToRuleJsonMessage(createCreateHearingPart());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"sessionId\":null,"
            + "\"caseType\":\"" + CASE_TYPE + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"scheduleStart\":\"" + START_MAPPED + "\","
            + "\"scheduleEnd\":\"" + END_MAPPED + "\","
            + "\"createdAt\":\"" + START_MAPPED + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    private CreateHearingPart createCreateHearingPart() {
        val chp = new CreateHearingPart();
        chp.setId(createUuid());
        chp.setDuration(createDuration());
        chp.setCaseType(CASE_TYPE);
        chp.setScheduleStart(START);
        chp.setScheduleEnd(END);
        chp.setCreatedAt(START);

        return chp;
    }

    @Test
    public void mapHearingPartToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapHearingPartToRuleJsonMessage(createHearingPart());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"sessionId\":\"" + ID + "\","
            + "\"caseType\":\"case-type\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"scheduleStart\":\"" + START_MAPPED + "\","
            + "\"scheduleEnd\":\"" + END_MAPPED + "\","
            + "\"createdAt\":\"" + START_MAPPED + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    private HearingPart createHearingPart() {
        val hp = new HearingPart();
        hp.setId(createUuid());
        hp.setDuration(createDuration());
        hp.setCaseType(CASE_TYPE);
        hp.setScheduleStart(START);
        hp.setScheduleEnd(END);
        hp.setCreatedAt(START);
        hp.setSession(createSession());

        return hp;
    }

    @Test
    public void mapDbSessionToRuleJsonMessage_mapsOk() {
        val mapped = factsMapper.mapDbSessionToRuleJsonMessage(createSession());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"caseType\":\"" + CASE_TYPE + "\""
            + "}";

        assertThat(mapped).isEqualTo(expected);

    }

    @Test
    public void mapDbHearingPartToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapDbHearingPartToRuleJsonMessage(createHearingPart());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"sessionId\":\"" + ID + "\","
            + "\"caseType\":\"" + CASE_TYPE + "\","
            + "\"duration\":" + DURATION_MAPPED + ","
            + "\"scheduleStart\":\"" + START_MAPPED + "\","
            + "\"scheduleEnd\":\"" + END_MAPPED + "\","
            + "\"createdAt\":\"" + START_MAPPED + "\""
            + "}";

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
    public void mapDbAvailabilityToRuleJsonMessage_mapsOk() throws JsonProcessingException {
        val mapped = factsMapper.mapDbAvailabilityToRuleJsonMessage(createAvailability());
        val expected = "{"
            + "\"id\":\"" + ID + "\","
            + "\"judgeId\":\"" + PERSON_ID + "\","
            + "\"roomId\":\"" + ROOM_ID + "\","
            + "\"start\":\"" + START_MAPPED + "\","
            + "\"duration\":" + DURATION_MAPPED
            + "}";

        assertThat(mapped).isEqualTo(expected);
    }

    private Availability createAvailability() {
        val a = new Availability();
        a.setId(createUuid());
        a.setDuration(createDuration());
        a.setStart(START);
        a.setPerson(createPerson());
        a.setRoom(createRoom());

        return a;
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
        us.setCaseType(CASE_TYPE);
        us.setRoomId(createUuid(ROOM_ID).toString());
        us.setPersonId(createUuid(PERSON_ID).toString());

        return us;
    }

    private Session createSession() {
        val s = new Session();
        s.setId(createUuid());
        s.setDuration(createDuration());
        s.setStart(START);
        s.setCaseType(CASE_TYPE);
        s.setPerson(createPerson());
        s.setRoom(createRoom());

        return s;
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