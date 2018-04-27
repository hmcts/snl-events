package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactSession;

import java.time.Duration;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

@Component
public class SessionMapper {
    private final ObjectMapper objectMapper;

    public SessionMapper() {
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

    public String mapSessionToRuleJsonMessage(CreateSession createSession) throws JsonProcessingException {
        FactSession factSession = new FactSession();
        factSession.setId(createSession.getId().toString());
        factSession.setDuration(createSession.getDuration());
        factSession.setStart(createSession.getStart());
        if (createSession.getPersonId() != null) {
            factSession.setJudgeId(createSession.getPersonId().toString());
        }

        if (createSession.getRoomId() != null) {
            factSession.setRoomId(createSession.getRoomId().toString());
        }

        return objectMapper.writeValueAsString(factSession);
    }
}
