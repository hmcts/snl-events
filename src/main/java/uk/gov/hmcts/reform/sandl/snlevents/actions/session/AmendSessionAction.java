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
import java.time.OffsetDateTime;
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

        sessionRepository.save(session);
    }

    private OffsetDateTime updateStartTimeFromRequest(OffsetDateTime startDateTime, String startTime) {
        val parts = startTime.split("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");
        val hour = Integer.valueOf(parts[0]);
        val minute = Integer.valueOf(parts[1]);

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
