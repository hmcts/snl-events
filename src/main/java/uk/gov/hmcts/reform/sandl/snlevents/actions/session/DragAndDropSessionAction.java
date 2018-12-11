package uk.gov.hmcts.reform.sandl.snlevents.actions.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.SessionWithHearingPartsFacts;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class DragAndDropSessionAction extends Action implements RulesProcessable {
    private DragAndDropSessionRequest dragAndDropSessionRequest;
    private SessionRepository sessionRepository;
    private RoomRepository roomRepository;
    private PersonRepository personRepository;
    private EntityManager entityManager;
    private Session session;
    private List<HearingPart> hearingParts;
    private String currentSessionAsString;

    public DragAndDropSessionAction(DragAndDropSessionRequest dragAndDropSessionRequest,
                                    SessionRepository sessionRepository,
                                    RoomRepository roomRepository,
                                    PersonRepository personRepository,
                                    EntityManager entityManager,
                                    ObjectMapper objectMapper) {
        this.dragAndDropSessionRequest = dragAndDropSessionRequest;
        this.sessionRepository = sessionRepository;
        this.roomRepository = roomRepository;
        this.personRepository = personRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        session  = sessionRepository.findOne(dragAndDropSessionRequest.getSessionId());
        hearingParts = session.getHearingParts();
        validateIfJudgeCanBeChanged(session, dragAndDropSessionRequest);
    }

    @Override
    public void act() {
        try {
            currentSessionAsString = objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new SnlEventsException("Given session couldn't be converted into string");
        }

        Session session = updateSession(dragAndDropSessionRequest);
        entityManager.detach(session);
        session.setVersion(dragAndDropSessionRequest.getVersion());

        sessionRepository.save(session);
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        SessionWithHearingPartsFacts sessionWithHpFacts =
            factsMapper.mapDbSessionToRuleJsonMessage(session, hearingParts);
        List<FactMessage> facts = sessionWithHpFacts.getHearingPartsFacts().stream()
            .map(hpFact -> new FactMessage(RulesService.UPSERT_HEARING_PART, hpFact))
            .collect(Collectors.toList());
        facts.add(new FactMessage(RulesService.UPSERT_SESSION, sessionWithHpFacts.getSessionFact()));

        return facts;
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

        for (HearingPart hp : hearingParts) {
            String hearingPartAsString = null;

            try {
                hearingPartAsString = objectMapper.writeValueAsString(hp);
            } catch (JsonProcessingException e) {
                throw new SnlRuntimeException("Could not convert hearing part to String. Hearing part id: "
                    + hp.getId());
            }

            UserTransactionData transactionData = new UserTransactionData("hearingPart",
                hp.getId(),
                hearingPartAsString,
                "lock",
                "unlock",
                0
            );
            userTransactionDataList.add(transactionData);
        }

        return userTransactionDataList;
    }

    @Override
    public UUID getUserTransactionId() {
        return dragAndDropSessionRequest.getUserTransactionId();
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        List<UUID> associatedEntitiesIds = new ArrayList<>();
        associatedEntitiesIds.add(dragAndDropSessionRequest.getSessionId());
        hearingParts.forEach(hp -> associatedEntitiesIds.add(hp.getId()));

        return associatedEntitiesIds.toArray(new UUID[0]);
    }

    private void validateIfJudgeCanBeChanged(Session session, DragAndDropSessionRequest dragAndDropSessionRequest) {
        boolean sessionHasMultiSessionHearingPart = session.getHearingParts().stream()
            .anyMatch(hp -> hp.getHearing().isMultiSession());

        UUID assignedPersonId = session.getPerson() != null ? session.getPerson().getId() : null;

        if (sessionHasMultiSessionHearingPart && !assignedPersonId.equals(dragAndDropSessionRequest.getPersonId())) {
            throw new SnlRuntimeException("This session cannot be assigned to a different judge "
                + "as it includes a multi-session hearing which needs the same judge throughout");
        }
    }

    private Session updateSession(DragAndDropSessionRequest dragAndDropSessionRequest) {
        session.setDuration(Duration.ofSeconds(dragAndDropSessionRequest.getDurationInSeconds()));
        session.setStart(dragAndDropSessionRequest.getStart());
        Room room = (dragAndDropSessionRequest.getRoomId() != null)
            ? roomRepository.findOne(dragAndDropSessionRequest.getRoomId()) : null;
        session.setRoom(room);
        Person person = (dragAndDropSessionRequest.getPersonId() != null)
            ? personRepository.findOne(dragAndDropSessionRequest.getPersonId()) : null;

        session.setPerson(person);

        return session;
    }
}
