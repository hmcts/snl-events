package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Availability;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactAvailability;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactPerson;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactRoom;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactTime;

import java.time.Duration;
import java.util.Optional;

import javax.xml.ws.WebServiceException;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

@Component
public class FactsMapper {
    private final ObjectMapper objectMapper;

    public FactsMapper() {
        objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        SimpleModule simpleModule = new SimpleModule("DurationToSecondsSerializer",
            new Version(1, 0, 0, null, null, null));
        simpleModule.addSerializer(Duration.class, new DurationToSecondsSerializer());
        objectMapper.registerModule(simpleModule);
    }

    public String mapCreateSessionToRuleJsonMessage(UpsertSession upsertSession) throws JsonProcessingException {
        FactSession factSession = new FactSession();

        factSession.setId(upsertSession.getId().toString());
        factSession.setDuration(upsertSession.getDuration());
        factSession.setStart(upsertSession.getStart());
        factSession.setSessionType(upsertSession.getSessionType());

        Optional.ofNullable(upsertSession.getRoomId()).ifPresent(factSession::setRoomId);
        Optional.ofNullable(upsertSession.getPersonId()).ifPresent(factSession::setJudgeId);

        return objectMapper.writeValueAsString(factSession);
    }

    public String mapUpdateSessionToRuleJsonMessage(Session session) throws JsonProcessingException {
        FactSession factSession = new FactSession();

        factSession.setId(session.getId().toString());
        factSession.setDuration(session.getDuration());
        factSession.setStart(session.getStart());
        if (session.getSessionType() != null) {
            factSession.setSessionType(session.getSessionType().getCode());
        }

        Optional.ofNullable(session.getRoom()).ifPresent(r -> factSession.setRoomId(r.getId().toString()));
        Optional.ofNullable(session.getPerson()).ifPresent(p -> factSession.setJudgeId(p.getId().toString()));

        return objectMapper.writeValueAsString(factSession);
    }

    public String mapHearingPartToRuleJsonMessage(HearingPart hearingPart) throws JsonProcessingException {
        return mapDbHearingPartToRuleJsonMessage(hearingPart);
    }

    public String mapDbSessionToRuleJsonMessage(Session session) {
        FactSession factSession = new FactSession();

        factSession.setId(session.getId().toString());
        factSession.setDuration(session.getDuration());
        factSession.setStart(session.getStart());
        if (session.getSessionType() != null) {
            factSession.setSessionType(session.getSessionType().getCode());
        }
        if (session.getPerson() != null) {
            factSession.setJudgeId(session.getPerson().getId().toString());
        }

        if (session.getRoom() != null) {
            factSession.setRoomId(session.getRoom().getId().toString());
        }

        try {
            return objectMapper.writeValueAsString(factSession);
        } catch (JsonProcessingException e) {
            throw new WebServiceException("Cannot map session to fact", e);
        }
    }

    public String mapDbHearingPartToRuleJsonMessage(HearingPart hearingPart) throws JsonProcessingException {
        FactHearingPart factHearingPart = new FactHearingPart();

        factHearingPart.setId(hearingPart.getId().toString());
        factHearingPart.setDuration(hearingPart.getDuration());
        factHearingPart.setCaseType(hearingPart.getCaseType());
        factHearingPart.setScheduleStart(hearingPart.getScheduleStart());
        factHearingPart.setScheduleEnd(hearingPart.getScheduleEnd());
        factHearingPart.setCreatedAt(hearingPart.getCreatedAt());
        if (hearingPart.getSession() != null) {
            factHearingPart.setSessionId(hearingPart.getSession().getId().toString());
        }

        Optional.ofNullable(hearingPart.getSession()).ifPresent(
            s -> factHearingPart.setSessionId(s.getId().toString()));

        return objectMapper.writeValueAsString(factHearingPart);
    }

    public String mapDbRoomToRuleJsonMessage(Room room) throws JsonProcessingException {
        FactRoom factRoom = new FactRoom();

        factRoom.setId(room.getId().toString());
        factRoom.setName(room.getName());

        return objectMapper.writeValueAsString(factRoom);
    }

    public String mapDbPersonToRuleJsonMessage(Person person) throws JsonProcessingException {
        FactPerson factPerson = new FactPerson();

        factPerson.setId(person.getId().toString());
        factPerson.setName(person.getName());

        return objectMapper.writeValueAsString(factPerson);
    }

    public String mapDbAvailabilityToRuleJsonMessage(Availability availability) throws JsonProcessingException {
        FactAvailability factAvailability = new FactAvailability();

        factAvailability.setId(availability.getId().toString());
        factAvailability.setDuration(availability.getDuration());
        factAvailability.setStart(availability.getStart());
        if (availability.getPerson() != null) {
            factAvailability.setJudgeId(availability.getPerson().getId().toString());
        }
        if (availability.getRoom() != null) {
            factAvailability.setRoomId(availability.getRoom().getId().toString());
        }

        return objectMapper.writeValueAsString(factAvailability);
    }

    public String mapTimeToRuleJsonMessage(DateTimePartValue dateTimePartValue) throws JsonProcessingException {
        FactTime factTime = new FactTime();
        factTime.setValue(dateTimePartValue.getValue());

        return objectMapper.writeValueAsString(factTime);
    }
}
