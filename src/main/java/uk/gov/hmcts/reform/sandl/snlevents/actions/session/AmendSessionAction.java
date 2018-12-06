package uk.gov.hmcts.reform.sandl.snlevents.actions.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.hibernate.Hibernate;
import org.hibernate.service.spi.ServiceException;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.helpers.UserTransactionDataPreparerService;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.SessionWithHearingPartsFacts;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class AmendSessionAction extends Action implements RulesProcessable {
    private AmendSessionRequest amendSessionRequest;
    private SessionRepository sessionRepository;
    private EntityManager entityManager;

    private Session session;
    private List<HearingPart> hearingParts;
    private String currentSessionAsString;
    private UserTransactionDataPreparerService userDatPrepServ = new UserTransactionDataPreparerService();

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
            throw new ServiceException("Given session couldn't be converted into string");
        }

        entityManager.detach(session);
        session.setSessionType(entityManager.getReference(SessionType.class, amendSessionRequest.getSessionTypeCode()));
        session.setDuration(amendSessionRequest.getDurationInSeconds());
        session.setStart(updateStartTimeFromRequest(session.getStart(), amendSessionRequest.getStartTime()));
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
        Hibernate.initialize(session.getHearingParts());
        hearingParts = session.getHearingParts();
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        SessionWithHearingPartsFacts sessionWithHpFacts =
            factsMapper.mapDbSessionToRuleJsonMessage(session, hearingParts);
        List<FactMessage> facts = sessionWithHpFacts.getHearingPartsFacts().stream().map(hpFact ->
            new FactMessage(RulesService.UPSERT_HEARING_PART, hpFact)
        ).collect(Collectors.toList());
        facts.add(new FactMessage(RulesService.UPSERT_SESSION, sessionWithHpFacts.getSessionFact()));

        return facts;
    }

    @Override
    public List<UserTransactionData> generateUserTransactionData() {
        userDatPrepServ.prepareUserTransactionDataForUpdate(userDatPrepServ.session, session.getId(),
            currentSessionAsString, 0);

        return userDatPrepServ.getUserTransactionDataList();
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
