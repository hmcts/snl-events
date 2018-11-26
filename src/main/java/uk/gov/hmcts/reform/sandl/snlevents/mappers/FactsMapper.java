package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactCaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactHearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactPerson;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactReloadStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactRoom;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactTime;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.SessionWithHearingPartsFacts;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public SessionWithHearingPartsFacts mapUpdateSessionToRuleJsonMessage(Session session,
                                                                          List<HearingPart> hearingParts) {
        return mapDbSessionToRuleJsonMessage(session, hearingParts);
    }

    public String mapHearingToRuleJsonMessage(HearingPart hearingPart) {
        return mapHearingPartToRuleJsonMessage(hearingPart);
    }

    public SessionWithHearingPartsFacts mapDbSessionToRuleJsonMessage(Session session, List<HearingPart> hearingParts) {
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

        String sessionMsg;
        List<String> hearingPartsMsg;
        try {
            sessionMsg = objectMapper.writeValueAsString(factSession);
            // its not possible to do session.getHearingParts() as session is often detached
            hearingPartsMsg = hearingParts.stream()
                .map(this::mapHearingPartToRuleJsonMessage)
                .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException("Cannot map session or its hearing part to fact " + e.getMessage());
        }

        return new SessionWithHearingPartsFacts(sessionMsg, hearingPartsMsg);
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

    public String mapHearingPartToRuleJsonMessage(HearingPart hearingPart) {
        FactHearingPart factHearingPart = new FactHearingPart();
        Hearing hearing = hearingPart.getHearing();

        factHearingPart.setId(hearingPart.getId().toString());

        if (hearing.isMultiSession() && hearingPart.getSession() != null) {
            factHearingPart.setDuration(hearingPart.getSession().getDuration());
        } else {
            factHearingPart.setDuration(hearing.getDuration());
        }

        factHearingPart.setCaseTypeCode(hearing.getCaseType().getCode());
        factHearingPart.setHearingTypeCode(hearing.getHearingType().getCode());
        factHearingPart.setScheduleStart(hearing.getScheduleStart());
        factHearingPart.setScheduleEnd(hearing.getScheduleEnd());
        factHearingPart.setCreatedAt(hearing.getCreatedAt());

        if (hearingPart.getSessionId() != null) {
            factHearingPart.setSessionId(hearingPart.getSessionId().toString());
        }

        try {
            return objectMapper.writeValueAsString(factHearingPart);
        } catch (JsonProcessingException e) {
            throw new SnlRuntimeException(e);
        }
    }

    public String mapReloadStatusToRuleJsonMessage(
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt) throws JsonProcessingException {

        FactReloadStatus factReloadStatus = new FactReloadStatus(startedAt, finishedAt);
        return objectMapper.writeValueAsString(factReloadStatus);
    }
}
