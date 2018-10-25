package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Availability;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactAvailability;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactCaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactPerson;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactRoom;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactTime;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
        factSession.setSessionType(upsertSession.getSessionTypeCode());

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

    public String mapHearingToRuleJsonMessage(HearingPart hearingPart) throws JsonProcessingException {
        FactHearingPart factHearingPart = new FactHearingPart();
        Hearing hearing = hearingPart.getHearing();

        factHearingPart.setId(hearingPart.getId().toString());
        factHearingPart.setDuration(hearing.getDuration());
        factHearingPart.setCaseTypeCode(hearing.getCaseType().getCode());
        factHearingPart.setHearingTypeCode(hearing.getHearingType().getCode());
        factHearingPart.setScheduleStart(hearing.getScheduleStart());
        factHearingPart.setScheduleEnd(hearing.getScheduleEnd());
        factHearingPart.setCreatedAt(hearing.getCreatedAt());

        if (hearingPart.getSessionId() != null) {
            factHearingPart.setSessionId(hearingPart.getSessionId().toString());
        }

        Optional.ofNullable(hearingPart.getSession()).ifPresent(
            s -> factHearingPart.setSessionId(s.getId().toString()));

        return objectMapper.writeValueAsString(factHearingPart);
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
            throw new WebServiceException("Cannot map session to ft", e);
        }
    }

    // to be removed?
    @Deprecated
    public String mapDbHearingToRuleJsonMessage(Hearing hearing) throws JsonProcessingException {
        FactHearingPart factHearingPart = new FactHearingPart();

        factHearingPart.setId(hearing.getId().toString());
        factHearingPart.setDuration(hearing.getDuration());
        factHearingPart.setCaseTypeCode(hearing.getCaseType().getCode());
        factHearingPart.setHearingTypeCode(hearing.getHearingType().getCode());
        factHearingPart.setScheduleStart(hearing.getScheduleStart());
        factHearingPart.setScheduleEnd(hearing.getScheduleEnd());
        factHearingPart.setCreatedAt(hearing.getCreatedAt());

        HearingPart hearingPart = hearing.getHearingParts().get(0); // @TODO temporary solution
        if (hearingPart.getSessionId() != null) {
            factHearingPart.setSessionId(hearingPart.getSessionId().toString());
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

    public String mapDbSessionTypeToRuleJsonMessage(SessionType sessionType) throws JsonProcessingException {
        FactSessionType factSessionType = new FactSessionType();
        factSessionType.setId(sessionType.getCode());
        List<FactCaseType> caseTypes = new ArrayList<>();
        for (val ct : sessionType.getCaseTypes()) {
            caseTypes.add(mapDbCaseTypeToFactCaseType(ct));
        }
        List<FactHearingType> hearingTypes = new ArrayList<>();
        for (val ht : sessionType.getHearingTypes()) {
            hearingTypes.add(mapDbHearingTypeToFactHearingType(ht));
        }

        factSessionType.setCaseTypes(caseTypes);
        factSessionType.setHearingTypes(hearingTypes);

        return objectMapper.writeValueAsString(factSessionType);
    }

    private FactCaseType mapDbCaseTypeToFactCaseType(CaseType ct) {
        return new FactCaseType(ct.getCode(), ct.getDescription());
    }

    private FactHearingType mapDbHearingTypeToFactHearingType(HearingType ht) {
        return new FactHearingType(ht.getCode(), ht.getDescription());
    }

    public String mapHearingPartToRuleJsonMessage(HearingPart hearingPart) throws JsonProcessingException {
        Hearing hearing = hearingPart.getHearing();
        FactHearingPart factHearingPart = new FactHearingPart();

        factHearingPart.setId(hearing.getId().toString());
        factHearingPart.setDuration(hearing.getDuration());
        factHearingPart.setCaseTypeCode(hearing.getCaseType().getCode());
        factHearingPart.setHearingTypeCode(hearing.getHearingType().getCode());
        factHearingPart.setScheduleStart(hearing.getScheduleStart());
        factHearingPart.setScheduleEnd(hearing.getScheduleEnd());
        factHearingPart.setCreatedAt(hearing.getCreatedAt());

        if (hearingPart.getSession() != null) {
            factHearingPart.setSessionId(hearingPart.getSession().getId().toString());
        }

        Optional.ofNullable(hearingPart.getSession()).ifPresent(
            s -> factHearingPart.setSessionId(s.getId().toString()));

        return objectMapper.writeValueAsString(factHearingPart);
    }
}
