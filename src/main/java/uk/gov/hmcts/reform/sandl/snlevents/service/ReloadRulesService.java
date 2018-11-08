package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationEngineConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DateTimePartValue;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactReloadStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.RulesEngineReloadStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReloadRulesService {
    private static final Logger logger = LoggerFactory.getLogger(ReloadRulesService.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private FactMessageService factMessageService;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactPropagationConfiguration factPropagationConfiguration;

    @Autowired
    private S2SRulesAuthenticationClient s2sAuthService;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private SessionTypeRepository sessionTypeRepository;

    @Autowired
    private Clock clock;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public List<RulesEngineReloadStatus> getReloadStatuses() {
        List<RulesEngineReloadStatus> reloadStatuses = new ArrayList<>();

        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        HttpEntity<FactMessage> entity = new HttpEntity<>(headers);

        for (val engine : factPropagationConfiguration.getEngines()) {
            FactReloadStatus reloadStatus = getFactReloadStatus(entity, engine);
            reloadStatuses.add(new RulesEngineReloadStatus(engine.getName(), reloadStatus));
        }

        return reloadStatuses;
    }

    public void reloadIfNeeded() throws IOException {
        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        HttpEntity<FactMessage> entity = new HttpEntity<>(headers);

        for (val engine : factPropagationConfiguration.getEngines()) {
            FactReloadStatus reloadStatus = getFactReloadStatus(entity, engine);
            // returns null if no status so it means it was never loaded
            // or if it is in loading, doesn't have finishedAt date then 60 minutes timeout to retry
            if (reloadStatus == null
                || (reloadStatus.getFinishedAt() == null)
                && reloadStatus.getStartedAt().isAfter(OffsetDateTime.now().plusMinutes(60))) {
                reloadRulesEngine(engine);
            }
        }
    }

    public void reloadDateAndTimeIfNeeded() throws JsonProcessingException {
        val now = OffsetDateTime.now(clock);
        if (now.getYear() != year) {
            sendDateTimePart("year", now.getYear());
            year = now.getYear();
        }
        if (now.getMonthValue() != month) {
            sendDateTimePart("month", now.getMonthValue());
            month = now.getMonthValue();
        }
        if (now.getDayOfMonth() != day) {
            sendDateTimePart("day", now.getDayOfMonth());
            day = now.getDayOfMonth();
        }
        if (now.getHour() != hour) {
            sendDateTimePart("hour", now.getHour());
            hour = now.getHour();
        }
        if (now.getMinute() != minute) {
            sendDateTimePart("minute", now.getMinute());
            minute = now.getMinute();
        }
    }

    private void sendDateTimePart(String timeType, int value) throws JsonProcessingException {
        logger.info("Loading rules with date/time part, " + timeType + ":" + value);
        val dateTimePartValue = new DateTimePartValue(timeType, value);
        rulesService.postMessage(String.format("upsert-%s", dateTimePartValue.getTimeType()),
            factsMapper.mapTimeToRuleJsonMessage(dateTimePartValue));
    }

    private void reloadRulesEngine(FactPropagationEngineConfiguration engine) throws IOException {
        logger.info("Loading rules with facts from db, " + engine.getName() + ", " + engine.getMsgUrl());

        OffsetDateTime startedAt = OffsetDateTime.now();
        updateReloadStatus(engine, startedAt, null);

        for (SessionType sessionType : sessionTypeRepository.findAll()) {
            String msg = factsMapper.mapDbSessionTypeToRuleJsonMessage(sessionType);
            postMessageToEngine(engine, RulesService.UPSERT_SESSION_TYPE, msg);
        }

        for (Room room : roomRepository.findAll()) {
            String msg = factsMapper.mapDbRoomToRuleJsonMessage(room);
            postMessageToEngine(engine, RulesService.UPSERT_ROOM, msg);
        }

        for (Person person : personRepository.findPeopleByPersonTypeEqualsIgnoreCase("judge")) {
            String msg = factsMapper.mapDbPersonToRuleJsonMessage(person);
            postMessageToEngine(engine, RulesService.UPSERT_JUDGE, msg);
        }

        for (Session session : sessionRepository.findAll()) {
            String msg = factsMapper.mapDbSessionToRuleJsonMessage(session);
            postMessageToEngine(engine, RulesService.UPSERT_SESSION, msg);
        }

        for (HearingPart hearingPart : hearingPartRepository.findAll()) {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hearingPart);
            postMessageToEngine(engine, RulesService.UPSERT_HEARING_PART, msg);
        }

        updateReloadStatus(engine, startedAt, OffsetDateTime.now());
        logger.info("Finished rules loading " + engine.getName());
    }

    private void updateReloadStatus(
        FactPropagationEngineConfiguration engine,
        OffsetDateTime startedAt, OffsetDateTime finishedAt) throws JsonProcessingException {
        String msg = factsMapper.mapReloadStatusToRuleJsonMessage(startedAt, finishedAt);
        postMessageToEngine(engine, RulesService.UPSERT_RELOAD_STATUS, msg);
    }

    @HystrixCommand
    private void postMessageToEngine(FactPropagationEngineConfiguration engine, String msgType, String msgData) {
        val entity = new FactMessage(msgType, msgData);
        ResponseEntity<String> factMsg = restTemplate.postForEntity(engine.getMsgUrl(), entity, String.class);
        factMessageService.handle(null, factMsg.getBody());
    }

    @HystrixCommand
    private FactReloadStatus getFactReloadStatus(
        HttpEntity<FactMessage> entity, FactPropagationEngineConfiguration engine) {
        return restTemplate
            .exchange(engine.getReloadStatusUrl(), HttpMethod.GET, entity, FactReloadStatus.class)
            .getBody();
    }
}
