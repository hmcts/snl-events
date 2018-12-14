package uk.gov.hmcts.reform.sandl.snlevents.actions.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.hibernate.service.spi.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.session.AmendSessionAction;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
public class AmendSessionActionTest {
    private static final String SESSION_TYPE_CODE = "TYPE";
    private static final Duration DURATION = Duration.ofMinutes(30);

    private static final UUID ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440001");
    private static final OffsetDateTime dateTime = OffsetDateTime.now();
    private static final SessionType SESSION_TYPE = new SessionType(SESSION_TYPE_CODE, "");

    private AmendSessionAction action;
    private AmendSessionRequest request;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        request = new AmendSessionRequest();
        request.setId(ID);
        request.setDurationInSeconds(DURATION);
        request.setSessionTypeCode(SESSION_TYPE_CODE);
        request.setStartTime(dateTime);
        request.setUserTransactionId(UUID.randomUUID());

        action = createAction(request);

        mockSessionRepositoryReturnsSession();
        mockEntityManagerReturnsReferenceToSessionType();
    }

    private void mockEntityManagerReturnsReferenceToSessionType() {
        Mockito.when(entityManager.getReference(SessionType.class, SESSION_TYPE_CODE))
            .thenReturn(SESSION_TYPE);
    }

    private SessionType createSessionType() {
        return new SessionType(SESSION_TYPE_CODE, "");
    }

    private void mockSessionRepositoryReturnsSession() {
        Session session = new Session();
        session.setId(ID);
        session.setStart(dateTime);
        Mockito.when(sessionRepository.findOne(ID)).thenReturn(session);
    }

    @Test
    public void act_savesSessionToRepository() {
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        action.getAndValidateEntities();
        action.act();

        Mockito.verify(sessionRepository).save(captor.capture());
        captor.getValue();
        assertThat(captor.getValue()).isEqualToComparingFieldByField(createExpectedSession());
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids).isEqualTo(new UUID[]{ID});
    }

    @Test
    public void generateFactMessages_shouldReturnOneElementWithTypeUpsertSession() {
        action.getAndValidateEntities();
        val factMsgs = action.generateFactMessages();

        assertThat(factMsgs.size()).isEqualTo(1);
        assertThat(factMsgs.get(0).getType()).isEqualTo(RulesService.UPSERT_SESSION);
    }

    @Test(expected = ServiceException.class)
    public void act_shouldThrowServiceException() throws JsonProcessingException {
        action.getAndValidateEntities();
        Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});
        action.act();
    }

    @Test
    public void getUserTransactionData_returnsCorrectData() {
        List<UserTransactionData> expectedTransactionData = new ArrayList<>();

        expectedTransactionData.add(new UserTransactionData("session",
            ID,
            null,
            "update",
            "update",
            0)
        );

        action.getAndValidateEntities();
        action.act();

        List<UserTransactionData> actualTransactionData = action.generateUserTransactionData();

        assertThat(actualTransactionData).isEqualTo(expectedTransactionData);
    }

    private Session createExpectedSession() {
        val s = new Session();
        s.setId(ID);
        s.setStart(dateTime);
        s.setSessionType(createSessionType());
        s.setDuration(DURATION);

        return s;
    }

    private AmendSessionAction createAction(AmendSessionRequest request) {
        return new AmendSessionAction(request, sessionRepository, entityManager, objectMapper);
    }
}
