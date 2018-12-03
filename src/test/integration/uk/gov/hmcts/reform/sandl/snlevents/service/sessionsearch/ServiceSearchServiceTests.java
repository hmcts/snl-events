package uk.gov.hmcts.reform.sandl.snlevents.service.sessionsearch;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionAmendResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ComparisonOperations;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SearchSessionSelectColumn;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SessionFilterKey;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@Transactional
@SuppressWarnings("PMD.VariableDeclarationUsageDistance")
public class ServiceSearchServiceTests extends BaseSessionSearchTest {
    private static final UUID JUDGE_KAMIL_ID = UUID.randomUUID();
    private static final String JUDGE_KAMIL_NAME = "Judge Kamil";
    private static final Person JUDGE_KAMIL = new Person();

    private static final String ROOM_TRYTON_ID = "d8104858-191a-47af-a427-cb907054ca93";
    private static final String ROOM_NAME = "TRYTON";
    private static final Room ROOM_TRYTON = new Room();

    private static final String ROOM_TYPE_CODE = "office";
    private static final String ROOM_TYPE_DESC = "office-desc";
    private static final RoomType ROOM_TYPE = new RoomType();

    private static final String YESTERDAY_DATE = "2018-11-10T10:13:11.293+01:00";
    private static final String NOW_DATE = "2018-11-11T10:13:11.293+01:00";
    private static final String TOMORROW_DATE = "2018-11-12T10:13:11.293+01:00";
    private static final OffsetDateTime YESTERDAY_DATETIME = OffsetDateTime.parse(YESTERDAY_DATE);
    private static final OffsetDateTime NOW_DATETIME = OffsetDateTime.parse(NOW_DATE);
    private static final OffsetDateTime TOMORROW_DATETIME = OffsetDateTime.parse(TOMORROW_DATE);

    private static final PageRequest SECOND_PAGE = new PageRequest(1, 10);

    @Autowired
    PersonRepository personRepository;

    @Autowired
    RoomTypeRepository roomTypeRepository;
    @Autowired
    RoomRepository roomRepository;

    @Before
    public void setup() {
        JUDGE_KAMIL.setName(JUDGE_KAMIL_NAME);
        JUDGE_KAMIL.setId(JUDGE_KAMIL_ID);
        personRepository.saveAndFlush(JUDGE_KAMIL);

        ROOM_TYPE.setCode(ROOM_TYPE_CODE);
        ROOM_TYPE.setDescription(ROOM_TYPE_DESC);
        roomTypeRepository.saveAndFlush(ROOM_TYPE);

        ROOM_TRYTON.setRoomType(ROOM_TYPE);
        ROOM_TRYTON.setId(UUID.fromString(ROOM_TRYTON_ID));
        ROOM_TRYTON.setName(ROOM_NAME);

        roomRepository.saveAndFlush(ROOM_TRYTON);
    }

    @Test
    public void search_byStartDate_returnSessionWhenStartDateIsAfterGivenDate() {
        Session tomorrowSession = createSession(null, null, null, null, TOMORROW_DATETIME);
        Session yesterdaySession = createSession(null, null, null, null, YESTERDAY_DATETIME);

        sessionRepository.saveAndFlush(tomorrowSession);
        sessionRepository.saveAndFlush(yesterdaySession);

        val startDateCriteria = new SearchCriteria();
        startDateCriteria.setKey(SessionFilterKey.START_DATE.getKey());
        startDateCriteria.setOperation(ComparisonOperations.EQUALS);
        startDateCriteria.setValue(NOW_DATE);
        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(startDateCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
    }

    @Test
    public void search_byEndDate_returnSessionWhenEndDateIsBeforeGivenDate() {
        Session tomorrowSession = createSession(null, null, null, null, TOMORROW_DATETIME);
        Session yesterdaySession = createSession(null, null, null, null, YESTERDAY_DATETIME);

        sessionRepository.saveAndFlush(tomorrowSession);
        sessionRepository.saveAndFlush(yesterdaySession);

        val startDateCriteria = new SearchCriteria();
        startDateCriteria.setKey(SessionFilterKey.END_DATE.getKey());
        startDateCriteria.setOperation(ComparisonOperations.EQUALS);
        startDateCriteria.setValue(NOW_DATE);
        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(startDateCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
    }

    @Test
    public void search_bySessionType_returnSessionWithSessionTypeCodeEqualsGivenInSearchCriteria() {
        UUID sessionId = UUID.randomUUID();
        Session sessionWithJudgeKamil = createSession(null, sessionId, null, null, null);
        sessionRepository.saveAndFlush(sessionWithJudgeKamil);

        val sessionTypeCriteria = new SearchCriteria();
        sessionTypeCriteria.setKey(SessionFilterKey.SESSION_TYPE_CODES.getKey());
        sessionTypeCriteria.setOperation(ComparisonOperations.IN);
        sessionTypeCriteria.setValue(Collections.singletonList(SESSION_TYPE_CODE));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(sessionTypeCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessions.getContent().get(0).getSessionId(), sessionId);
    }

    @Test
    public void search_byRoomId_IN_returnSessionWithRoomIdEqualsGivenInSearchCriteria() {
        Session sessionWithRoomTryton = createSession(null, null, null, ROOM_TRYTON, null);
        Session sessionWithoutRoom = createSession(null, null, null, null, null);

        sessionRepository.saveAndFlush(sessionWithRoomTryton);
        sessionRepository.saveAndFlush(sessionWithoutRoom);

        val roomCriteria = new SearchCriteria();
        roomCriteria.setKey(SessionFilterKey.ROOM_IDS.getKey());
        roomCriteria.setOperation(ComparisonOperations.IN);
        roomCriteria.setValue(Collections.singletonList(ROOM_TRYTON_ID));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(roomCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
        sessions.forEach(s -> {
            assertEquals(ROOM_NAME, s.getRoomName());
        });
    }

    @Test
    public void search_byRoomId_In_Or_Null_returnSessionWithRoomIdEqualsGivenInSearchCriteriaOrNull() {
        UUID sessionWithRoomId = UUID.randomUUID();
        Session sessionWithRoom = createSession(null, sessionWithRoomId, null, ROOM_TRYTON, null);
        UUID sessionIdWithoutRoom = UUID.randomUUID();
        Session sessionWithoutRoom = createSession(null, sessionIdWithoutRoom, null, null, null);

        sessionRepository.saveAndFlush(sessionWithRoom);
        sessionRepository.saveAndFlush(sessionWithoutRoom);

        val roomCriteria = new SearchCriteria();
        roomCriteria.setKey(SessionFilterKey.ROOM_IDS.getKey());
        roomCriteria.setOperation(ComparisonOperations.IN_OR_NULL);
        roomCriteria.setValue(Collections.singletonList(ROOM_TRYTON_ID));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(roomCriteria), FIRST_PAGE, null, null);

        assertEquals(2, sessions.getNumberOfElements());
        val sessionIds = sessions.getContent().stream()
            .map(SessionSearchResponse::getSessionId)
            .collect(Collectors.toList());
        assertTrue(sessionIds.contains(sessionWithRoomId));
        assertTrue(sessionIds.contains(sessionIdWithoutRoom));
    }

    @Test
    public void search_byPersonId_IN_returnSessionWithPersonIdEqualsGivenInSearchCriteria() {
        UUID sessionIdWithJudgeKamil = UUID.randomUUID();
        Session sessionWithJudgeKamil = createSession(null, sessionIdWithJudgeKamil, JUDGE_KAMIL, null, null);
        UUID sessionIdWithoutJudge = UUID.randomUUID();
        Session sessionWithoutJudge = createSession(null, sessionIdWithoutJudge, null, null, null);

        sessionRepository.saveAndFlush(sessionWithJudgeKamil);
        sessionRepository.saveAndFlush(sessionWithoutJudge);

        val personCriteria = new SearchCriteria();
        personCriteria.setKey(SessionFilterKey.PERSON_IDS.getKey());
        personCriteria.setOperation(ComparisonOperations.IN);
        personCriteria.setValue(Collections.singletonList(JUDGE_KAMIL_ID.toString()));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(personCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
        sessions.forEach(s -> {
            assertEquals(JUDGE_KAMIL_NAME, s.getPersonName());
        });
    }

    @Test
    public void search_byPersonId_In_Or_Null_returnSessionWithPersonIdEqualsGivenInSearchCriteriaOrNull() {
        UUID sessionIdWithJudgeKamil = UUID.randomUUID();
        Session sessionWithJudgeKamil = createSession(null, sessionIdWithJudgeKamil, JUDGE_KAMIL, null, null);
        UUID sessionIdWithoutJudge = UUID.randomUUID();
        Session sessionWithoutJudge = createSession(null, sessionIdWithoutJudge, null, null, null);

        sessionRepository.saveAndFlush(sessionWithJudgeKamil);
        sessionRepository.saveAndFlush(sessionWithoutJudge);

        val personCriteria = new SearchCriteria();
        personCriteria.setKey(SessionFilterKey.PERSON_IDS.getKey());
        personCriteria.setOperation(ComparisonOperations.IN_OR_NULL);
        personCriteria.setValue(Collections.singletonList(JUDGE_KAMIL_ID.toString()));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(personCriteria), FIRST_PAGE, null, null);

        assertEquals(2, sessions.getNumberOfElements());
    }

    @Test
    public void search_byUnlisted_returnSessionWithUtilisationEqualsZero() {
        Session sessionWithSingleHearingPart = createSession(HALF_HOUR, null, null, null, null);
        UUID sessionIdWithoutHearingPart = UUID.randomUUID();
        final Session sessionWithoutHearingPart = createSession(
            HALF_HOUR,
            sessionIdWithoutHearingPart,
            null,
            null,
            null
        );

        // When assign hearing part to session it should set utilisation
        Hearing hearing = createHearing(HALF_HOUR, null, false);
        UUID hearingPartId = UUID.randomUUID();
        HearingPart hearingPart = createHearingPart(UUID.randomUUID());
        hearingPart.setId(hearingPartId);
        hearingPart.setHearing(hearing);
        hearingPart.setSession(sessionWithSingleHearingPart);

        sessionRepository.saveAndFlush(sessionWithoutHearingPart);
        sessionRepository.saveAndFlush(sessionWithSingleHearingPart);
        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);

        val unlistedCriteria = new SearchCriteria();
        unlistedCriteria.setKey(SessionFilterKey.UNLISTED.getKey());

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(unlistedCriteria), FIRST_PAGE, null, null);

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessionIdWithoutHearingPart, sessions.getContent().get(0).getSessionId());
    }

    @Test
    public void search_byPartListed_returnSessionWithUtilisationBetween1And99() {
        UUID sessionIdWithHearingPart = UUID.randomUUID();
        final Session sessionWithSingleHearingPart = createSession(
            ONE_HOUR,
            sessionIdWithHearingPart,
            null,
            null,
            null
        );
        final Session sessionWithoutHearingPart = createSession(HALF_HOUR, UUID.randomUUID(), null, null, null);

        // When assign hearing part to session it should set utilisation
        Hearing hearing = createHearing(HALF_HOUR, null, false);
        UUID hearingPartId = UUID.randomUUID();
        HearingPart hearingPart = createHearingPart(hearingPartId);
        hearingPart.setHearing(hearing);
        hearingPart.setSession(sessionWithSingleHearingPart);

        sessionRepository.saveAndFlush(sessionWithoutHearingPart);
        sessionRepository.saveAndFlush(sessionWithSingleHearingPart);
        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);

        val unlistedCriteria = new SearchCriteria();
        unlistedCriteria.setKey(SessionFilterKey.PART_LISTED.getKey());

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(unlistedCriteria), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        long utilisation = sessionSearchResponse.getUtilisation();

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessionIdWithHearingPart, sessionSearchResponse.getSessionId());
        assertTrue(utilisation > 0 && utilisation < 100);
    }

    @Test
    public void search_byFullyListed_returnSessionWithUtilisationEquals100() {
        UUID sessionIdWithHearingPart = UUID.randomUUID();
        Session sessionWithSingleHearingPart = createSession(ONE_HOUR, sessionIdWithHearingPart, null, null, null);
        Session sessionWithoutHearingPart = createSession(ONE_HOUR, UUID.randomUUID(), null, null, null);
        sessionRepository.saveAndFlush(sessionWithoutHearingPart);
        sessionRepository.saveAndFlush(sessionWithSingleHearingPart);

        Hearing hearing = createHearing(ONE_HOUR, null, false);
        UUID hearingPartId = UUID.randomUUID();
        HearingPart hearingPart = createHearingPart(hearingPartId);
        hearingPart.setHearing(hearing);
        hearingPart.setSession(sessionWithSingleHearingPart);

        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);

        val unlistedCriteria = new SearchCriteria();
        unlistedCriteria.setKey(SessionFilterKey.FULLY_LISTED.getKey());

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(unlistedCriteria), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        long utilisation = sessionSearchResponse.getUtilisation();

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessionIdWithHearingPart, sessionSearchResponse.getSessionId());
        assertEquals(100, utilisation);
    }

    @Test
    public void search_byOverListed_returnSessionWithUtilisationGraterThan100() {
        UUID sessionIdWithHearingPart = UUID.randomUUID();
        Session sessionWithSingleHearingPart = createSession(HALF_HOUR, sessionIdWithHearingPart, null, null, null);
        final Session sessionWithoutHearingPart = createSession(HALF_HOUR, UUID.randomUUID(), null, null, null);

        Hearing hearing = createHearing(ONE_HOUR, null, false);
        UUID hearingPartId = UUID.randomUUID();
        HearingPart hearingPart = createHearingPart(hearingPartId);
        hearingPart.setHearing(hearing);
        hearingPart.setSession(sessionWithSingleHearingPart);

        sessionRepository.saveAndFlush(sessionWithoutHearingPart);
        sessionRepository.saveAndFlush(sessionWithSingleHearingPart);
        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);

        val unlistedCriteria = new SearchCriteria();
        unlistedCriteria.setKey(SessionFilterKey.OVER_LISTED.getKey());

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(unlistedCriteria), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        long utilisation = sessionSearchResponse.getUtilisation();

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessionIdWithHearingPart, sessionSearchResponse.getSessionId());
        assertTrue(utilisation > 100);
    }

    @Test
    public void search_forAmendment_shouldNotCountDeletedHearingPartInSession() {
        UUID sessionIdWithHearingParts = UUID.randomUUID();

        final Session session = createSession(
            ONE_HOUR,
            sessionIdWithHearingParts,
            null,
            null,
            null
        );

        Hearing hearing = createHearing(HALF_HOUR, null, false);
        HearingPart hearingPart = createHearingPart(UUID.randomUUID());
        hearingPart.setHearing(hearing);
        hearingPart.setSession(session);

        HearingPart hearingPartDeleted = createHearingPart(UUID.randomUUID());
        hearingPartDeleted.setHearing(hearing);
        hearingPartDeleted.setSession(session);
        hearingPartDeleted.setDeleted(true);

        sessionRepository.saveAndFlush(session);
        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);
        hearingPartRepository.saveAndFlush(hearingPartDeleted);

        SessionAmendResponse response = sessionService.getAmendSession(sessionIdWithHearingParts);
        assertEquals(1, response.getHearingPartsCount());
    }

    @Test
    public void search_byCustom_returnSessionWithUtilisationThatMatchGivenValues() {
        UUID sessionIdWithHearingPart = UUID.randomUUID();
        final Session sessionWithSingleHearingPart = createSession(
            ONE_HOUR,
            sessionIdWithHearingPart,
            null,
            null,
            null
        );
        final Session sessionWithoutHearingPart = createSession(HALF_HOUR, UUID.randomUUID(), null, null, null);

        Hearing hearing = createHearing(HALF_HOUR, null, false);
        UUID hearingPartId = UUID.randomUUID();
        HearingPart hearingPart = createHearingPart(hearingPartId);
        hearingPart.setHearing(hearing);
        hearingPart.setSession(sessionWithSingleHearingPart);

        sessionRepository.saveAndFlush(sessionWithoutHearingPart);
        sessionRepository.saveAndFlush(sessionWithSingleHearingPart);
        hearingRepository.saveAndFlush(hearing);
        hearingPartRepository.saveAndFlush(hearingPart);

        Integer customFrom = 49;
        Integer customTo = 51;
        val unlistedCriteria = new SearchCriteria();
        unlistedCriteria.setKey(SessionFilterKey.CUSTOM.getKey());
        unlistedCriteria.setValue(Arrays.asList(customFrom, customTo));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.singletonList(unlistedCriteria), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        long utilisation = sessionSearchResponse.getUtilisation();

        assertEquals(1, sessions.getNumberOfElements());
        assertEquals(sessionIdWithHearingPart, sessionSearchResponse.getSessionId());
        assertTrue(utilisation > customFrom && utilisation < customTo);
    }

    @Test
    public void search_Pagination() {
        List<UUID> uuids = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            UUID uuid = UUID.randomUUID();
            Session session = createSession(ONE_HOUR, uuid, null, null, null);
            sessionRepository.saveAndFlush(session);
            uuids.add(uuid);
        }

        Page<SessionSearchResponse> firstPageSessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, null, null);
        Page<SessionSearchResponse> secondPageSessions = sessionService.searchForSession(
            Collections.emptyList(), SECOND_PAGE, null, null);

        val fistPageSessionIds = firstPageSessions.getContent().stream()
            .map(SessionSearchResponse::getSessionId)
            .collect(Collectors.toList());
        val secondPageSessionIds = secondPageSessions.getContent().stream()
            .map(SessionSearchResponse::getSessionId)
            .collect(Collectors.toList());

        uuids.removeAll(fistPageSessionIds);
        uuids.removeAll(secondPageSessionIds);

        assertTrue(uuids.isEmpty());
    }

    @Test
    public void search_SortingByStartDate_Asc_ShouldReturnTheOldestFirst() {
        Session yesterdaySession = createSession(ONE_HOUR, null, null, null, YESTERDAY_DATETIME);
        Session nowSession = createSession(ONE_HOUR, null, null, null, NOW_DATETIME);
        Session tomorrowSession = createSession(ONE_HOUR, null, null, null, TOMORROW_DATETIME);

        sessionRepository.saveAndFlush(nowSession);
        sessionRepository.saveAndFlush(tomorrowSession);
        sessionRepository.saveAndFlush(yesterdaySession);

        List<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.SESSION_START_DATE, Sort.Direction.ASC)
            .getContent();

        assertTrue(sessions.get(0).getStartDate().isEqual(YESTERDAY_DATETIME));
        assertTrue(sessions.get(1).getStartDate().isEqual(NOW_DATETIME));
        assertTrue(sessions.get(2).getStartDate().isEqual(TOMORROW_DATETIME));
    }

    @Test
    public void search_SortingByStartDate_Desc_ShouldReturnTheLatestFirst() {
        Session yesterdaySession = createSession(ONE_HOUR, null, null, null, YESTERDAY_DATETIME);
        Session nowSession = createSession(ONE_HOUR, null, null, null, NOW_DATETIME);
        Session tomorrowSession = createSession(ONE_HOUR, null, null, null, TOMORROW_DATETIME);

        sessionRepository.saveAndFlush(nowSession);
        sessionRepository.saveAndFlush(tomorrowSession);
        sessionRepository.saveAndFlush(yesterdaySession);

        List<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.SESSION_START_DATE, Sort.Direction.DESC)
            .getContent();

        assertTrue(sessions.get(0).getStartDate().isEqual(TOMORROW_DATETIME));
        assertTrue(sessions.get(1).getStartDate().isEqual(NOW_DATETIME));
        assertTrue(sessions.get(2).getStartDate().isEqual(YESTERDAY_DATETIME));
    }
}
