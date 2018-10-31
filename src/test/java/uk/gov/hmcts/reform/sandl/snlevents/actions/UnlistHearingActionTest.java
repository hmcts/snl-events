package uk.gov.hmcts.reform.sandl.snlevents.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.UnlistHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class UnlistHearingActionTest {
    private static final UUID HEARING_ID_TO_BE_UNLISTED = UUID.randomUUID();
    private static final UUID HEARING_PART_ID_A = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_A = 1L;
    private static final UUID HEARING_PART_ID_B = UUID.randomUUID();
    private static final Long HEARING_VERSION_ID_B = 2L;
    private static final UUID SESSION_ID_A = UUID.randomUUID();
    private static final UUID SESSION_ID_B = UUID.randomUUID();
    private static final List<VersionInfo> hearingVersions = Arrays.asList(
        getVersionInfo(HEARING_PART_ID_A, HEARING_VERSION_ID_A),
        getVersionInfo(HEARING_PART_ID_B, HEARING_VERSION_ID_B)
    );

    private UnlistHearingAction action;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private ObjectMapper objectMapper;


    @Before
    public void setup() {
        Hearing hearing = new Hearing();
        hearing.setDuration(Duration.ofMinutes(60));
        hearing.setCaseType(new CaseType("cs-code", "cs-desc"));
        hearing.setHearingType(new HearingType("ht-code", "ht-desc"));
        hearing.setId(HEARING_ID_TO_BE_UNLISTED);
        hearing.setHearingParts(Arrays.asList(
            createHearingPartWithSession(HEARING_PART_ID_A, HEARING_VERSION_ID_A, hearing, SESSION_ID_A),
            createHearingPartWithSession(HEARING_PART_ID_B, HEARING_VERSION_ID_B, hearing, SESSION_ID_B)
        ));

        when(hearingRepository.findOne(eq(HEARING_ID_TO_BE_UNLISTED))).thenReturn(hearing);

        UnlistHearingRequest uhr = new UnlistHearingRequest();
        uhr.setHearingId(HEARING_ID_TO_BE_UNLISTED);
        uhr.setUserTransactionId(UUID.randomUUID());

        uhr.setHearingPartsVersions(hearingVersions);

        action = new UnlistHearingAction(
            uhr, hearingRepository, hearingPartRepository, objectMapper
        );
    }

    private HearingPart createHearingPartWithSession(UUID id, Long version, Hearing hearing, UUID sessionId) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(id);
        hearingPart.setHearing(hearing);
        hearingPart.setVersion(version);

        Session session = new Session();
        session.setId(sessionId);

        hearingPart.setSessionId(sessionId);
        hearingPart.setSession(session);

        return hearingPart;
    }

    @Test
    public void getAssociatedEntitiesIds_returnsCorrectIds() {
        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();
        UUID[] expectedUUIDs = new UUID[]{
            HEARING_ID_TO_BE_UNLISTED,
            HEARING_PART_ID_A,
            HEARING_PART_ID_B,
            SESSION_ID_A,
            SESSION_ID_B
        };

        assertThat(ids.length).isEqualTo(expectedUUIDs.length);
        assertTrue(Arrays.asList(ids).containsAll(Arrays.asList(expectedUUIDs)));
    }

    @Test
    public void act_shouldSetHearingPartSessionIdToNull() {
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);

        Mockito.verify(hearingPartRepository).save(captor.capture());
        assertThat(captor.getValue().size()).isEqualTo(hearingVersions.size());
        captor.getValue().forEach(hp -> {
            assertNull(hp.getSessionId());
            assertNull(hp.getSession());
            assertNull(hp.getStart());
        });
    }

    @Test
    public void getUserTransactionId_shouldReturnUUID() {
        assertNotNull(action.getUserTransactionId());
    }

    @Test
    public void generateUserTransactionData_shouldSetHearingPartSessionIdToNull() {
        action.getAndValidateEntities();
        action.act();
        List<UserTransactionData> userTransactionData = action.generateUserTransactionData();

        assertThatContainsHearigPart(userTransactionData, HEARING_PART_ID_A);
        assertThatContainsHearigPart(userTransactionData, HEARING_PART_ID_B);
        assertThatContainsEntity(userTransactionData, "hearing", HEARING_ID_TO_BE_UNLISTED);
        assertThatContainsEntity(userTransactionData, "session", SESSION_ID_A);
        assertThatContainsEntity(userTransactionData, "session", SESSION_ID_B);
    }

    private void assertThatContainsHearigPart(List<UserTransactionData> userTransactionData, UUID hearingPartId) {
        val hearingBUSerTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == hearingPartId).findFirst().get();
        assertThat(hearingBUSerTransactionData.getEntity()).isEqualTo("hearingPart");
        assertThat(hearingBUSerTransactionData.getAction()).isEqualTo("update");
        assertThat(hearingBUSerTransactionData.getCounterAction()).isEqualTo("update");
    }

    private void assertThatContainsEntity(List<UserTransactionData> userTransactionData, String entity, UUID id) {
        val hearingBUSerTransactionData = userTransactionData.stream().filter(utd -> utd.getEntityId() == id).findFirst().get();
        assertThat(hearingBUSerTransactionData.getEntity()).isEqualTo(entity);
        assertThat(hearingBUSerTransactionData.getAction()).isEqualTo("lock");
        assertThat(hearingBUSerTransactionData.getCounterAction()).isEqualTo("unlock");
    }

    private static VersionInfo getVersionInfo(UUID id, Long version) {
        VersionInfo vi = new VersionInfo();
        vi.setId(id);
        vi.setVersion(version);

        return vi;
    }
}
