package uk.gov.hmcts.reform.sandl.snlevents.actions.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

public class AmendSessionAction extends Action implements RulesProcessable {
    private AmendSessionRequest amendSessionRequest;
    private SessionRepository sessionRepository;
    private EntityManager entityManager;
    private ObjectMapper objectMapper;

    private Session session;
    private String currentSessionAsString;

    public AmendSessionAction(AmendSessionRequest amendSessionRequest,
                              SessionRepository sessionRepository,
                              EntityManager entityManager,
                              ObjectMapper objectMapper) {
        this.amendSessionRequest = amendSessionRequest;
        this.sessionRepository = sessionRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void act() {
        try {
            currentSessionAsString = objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        session.setSessionType(entityManager.getReference(SessionType.class, amendSessionRequest.getSessionTypeCode()));
        session.setDuration(Duration.ofSeconds(amendSessionRequest.getDurationInSeconds()));
        session.setStart(updateStartTimeFromRequest(session.getStart(), amendSessionRequest.getStartTime()));
        entityManager.detach(session);
        session.setVersion(amendSessionRequest.getVersion());

        sessionRepository.save(session);
    }

    private OffsetDateTime updateStartTimeFromRequest(OffsetDateTime startDateTime, String startTime) {
        val localTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        val hour = localTime.get(ChronoField.CLOCK_HOUR_OF_DAY);
        val minute = localTime.get(ChronoField.MINUTE_OF_HOUR);

        return startDateTime.withHour(hour).withMinute(minute);
    }

    @Override
    public void getAndValidateEntities() {
        session  = sessionRepository.findOne(amendSessionRequest.getId());
    }

    @Override
    public FactMessage generateFactMessage() {
        val msg = factsMapper.mapDbSessionToRuleJsonMessage(session);

        return new FactMessage(RulesService.UPSERT_SESSION, msg);
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("session",
            session.getId(),
            currentSessionAsString,
            "update",
            "update",
            0)
        );

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return amendSessionRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] { amendSessionRequest.getId() };
    }
}
