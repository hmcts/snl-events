package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ComparisonOperations;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Transactional
public class ServiceSearchServiceTests extends BaseIntegrationTest {
    public static final String CASE_NUMBER_123 = "123";
    public static final String SMALL_CLAIMS = "small-claims";
    public static final String FAST_TRACK = "fast-track";
    public static final String CASE_TYPE_FIELD = "caseType";
    public static final String CASE_TITLE_FIELD = "caseTitle";
    public static final String CASE_NUMBER_FIELD = "caseNumber";
    public static final String CASE_NUMBER_222 = "222";
    public static final String JUDGE_ID = "1143b1ea-1813-4acc-8b08-f37d1db59492";
    public static final UUID JUDGE_KAMIL_ID = UUID.randomUUID();
    public static final String JUDGE_KAMIL_NAME = "Judge Kamil";

    public static final Person JUDGE_KAMIL = new Person();

    public static final String ROOM_TRYTON_ID = "d8104858-191a-47af-a427-cb907054ca93";
    public static final String ROOM_NAME = "TRYTON";
    public static final Room ROOM_TRYTON = new Room();

    public static final String YESTERDAY_DATE = "2018-11-10T10:13:11.293+01:00";
    public static final String NOW_DATE = "2018-11-11T10:13:11.293+01:00";
    public static final String TOMORROW_DATE = "2018-11-12T10:13:11.293+01:00";
    public static final OffsetDateTime YESTERDAY_DATETIME = OffsetDateTime.parse(YESTERDAY_DATE);
    public static final OffsetDateTime TOMORROW_DATETIME = OffsetDateTime.parse(TOMORROW_DATE);
    public static final String ROOM_TYPE_CODE = "office";
    public static final String ROOM_TYPE_DESC = "office-desc";


    private final CaseType smallClaims = new CaseType(SMALL_CLAIMS, "SC");
    private final CaseType fastTrack = new CaseType(FAST_TRACK, "FT");
    private final HearingType trial = new HearingType("trial", "Trial");

    public static final String SESSION_TYPE_CODE = "small-trial";
    private final SessionType SMALL_TRIAL_SESSION_TYPE = new SessionType(SESSION_TYPE_CODE, "ST");


    private final PageRequest firstPage = new PageRequest(0, 10);

    @Autowired
    HearingRepository hearingRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    HearingPartRepository hearingPartRepository;
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;
    @Autowired
    RoomTypeRepository roomTypeRepository;
    @Autowired
    RoomRepository roomRepository;

    @Autowired
    SessionService sessionService;
    public static final RoomType ROOM_TYPE = new RoomType();

    private Session createSession(Duration duration, UUID uuid, Person person, Room room, OffsetDateTime start) {
        Duration defaultDuration = Duration.ofMinutes(30);

        if (duration == null) {
            duration = defaultDuration;
        }

        UUID id = uuid;
        if (uuid == null) {
            id = UUID.randomUUID();
        }

        OffsetDateTime startTime = start;
        if (start == null) {
            startTime = OffsetDateTime.now();
        }

        Session session = new Session();
        session.setId(id);
        session.setSessionType(SMALL_TRIAL_SESSION_TYPE);
        session.setDuration(duration);
        session.setStart(startTime);
        session.setPerson(person);
        session.setRoom(room);

        return session;
    }

    private Hearing createHearing(Duration duration, UUID uuid, Boolean isMultisession) {
        UUID id = uuid;
        if (uuid == null) {
            id = UUID.randomUUID();
        }

        Hearing hearing = new Hearing();
        hearing.setId(id);
//        hearing.setCaseNumber("singleHearing");
//        hearing.setCaseTitle("singleHearing");
//        hearing.setPriority(Priority.Low);
        hearing.setCaseType(smallClaims);
        hearing.setHearingType(trial);
        hearing.setDuration(duration);
        hearing.setMultiSession(isMultisession);

        return hearing;
    }

//    private HearingPart createHearingPart(Duration duration, UUID uuid, Boolean isMultisession) {
//        UUID id = uuid;
//        if (uuid == null) {
//            id = UUID.randomUUID();
//        }
//
//        final HearingPart multiHearingPart = new HearingPart();
//        multiHearingPart.setId(UUID.randomUUID());
//        multiHearingPart.setSessionId(multiSession.getId());
//        multiHearingPart.setHearingId(multiHearing.getId());
//        multiSession.addHearingPart(multiHearingPart);
//
//        return multiHearingPart;
//    }

    @Before
    public void setup() {
        // Single session
//        final Session singleSession = new Session();
//        singleSession.setId(UUID.randomUUID());
//        singleSession.setSessionType(SMALL_TRIAL_SESSION_TYPE);
//        singleSession.setDuration(Duration.ofHours(1));
//        singleSession.setStart(OffsetDateTime.now());
//
//        final Hearing singleHearing = new Hearing();
//        singleHearing.setId(UUID.randomUUID());
//        singleHearing.setCaseNumber("singleHearing");
//        singleHearing.setCaseTitle("singleHearing");
//        singleHearing.setPriority(Priority.Low);
//        singleHearing.setCaseType(smallClaims);
//        singleHearing.setHearingType(trial);
//        singleHearing.setDuration(Duration.ofMinutes(45));
//        singleHearing.setMultiSession(false);
//
//        final HearingPart singleHearingPart = new HearingPart();
//        singleHearingPart.setId(UUID.randomUUID());
//        singleHearingPart.setSessionId(singleSession.getId());
//        singleHearingPart.setHearingId(singleHearing.getId());
//        singleSession.addHearingPart(singleHearingPart);
//
//        singleHearing.addHearingPart(singleHearingPart);
//
//        // Multi session example
//        final Session multiSession = new Session();
//        multiSession.setId(UUID.randomUUID());
//        multiSession.setSessionType(SMALL_TRIAL_SESSION_TYPE);
//        multiSession.setDuration(Duration.ofMinutes(120));
//        multiSession.setStart(OffsetDateTime.now());
//
//        final Session multiSessionExtra = new Session();
//        multiSessionExtra.setId(UUID.randomUUID());
//        multiSessionExtra.setSessionType(SMALL_TRIAL_SESSION_TYPE);
//        multiSessionExtra.setDuration(Duration.ofMinutes(20));
//        multiSessionExtra.setStart(OffsetDateTime.now());
//
//        final Hearing multiHearing = new Hearing();
//        multiHearing.setId(UUID.randomUUID());
//        multiHearing.setCaseNumber("multiHearing");
//        multiHearing.setCaseTitle("multiHearing");
//        multiHearing.setPriority(Priority.Low);
//        multiHearing.setCaseType(smallClaims);
//        multiHearing.setHearingType(trial);
//        multiHearing.setDuration(Duration.ofMinutes(0));
//        multiHearing.setMultiSession(true);
//
//        final HearingPart multiHearingPart = new HearingPart();
//        multiHearingPart.setId(UUID.randomUUID());
//        multiHearingPart.setSessionId(multiSession.getId());
//        multiHearingPart.setHearingId(multiHearing.getId());
//        multiSession.addHearingPart(multiHearingPart);
//
//        final HearingPart multiHearingPartExtra = new HearingPart();
//        multiHearingPartExtra.setId(UUID.randomUUID());
//        multiHearingPartExtra.setSessionId(multiSessionExtra.getId());
//        multiHearingPartExtra.setHearingId(multiHearing.getId());
//        multiSessionExtra.addHearingPart(multiHearingPartExtra);
//
//        // Mix
//        final Session mixSession = new Session();
//        mixSession.setId(UUID.randomUUID());
//        mixSession.setSessionType(SMALL_TRIAL_SESSION_TYPE);
//        mixSession.setDuration(Duration.ofMinutes(150));
//        mixSession.setStart(OffsetDateTime.now());
//
//        final Hearing mixSingleHearing = new Hearing();
//        mixSingleHearing.setId(UUID.randomUUID());
//        mixSingleHearing.setCaseNumber(CASE_NUMBER_222);
//        mixSingleHearing.setCaseTitle("mixSingleHearing");
//        mixSingleHearing.setPriority(Priority.Low);
//        mixSingleHearing.setCaseType(fastTrack);
//        mixSingleHearing.setHearingType(trial);
//        mixSingleHearing.setMultiSession(false);
//        mixSingleHearing.setDuration(Duration.ofMinutes(75));
//
//        final HearingPart mixSingleHearingPart = new HearingPart();
//        mixSingleHearingPart.setId(UUID.randomUUID());
//        mixSingleHearingPart.setSessionId(mixSession.getId());
//        mixSingleHearingPart.setHearingId(mixSingleHearing.getId());
//        mixSession.addHearingPart(mixSingleHearingPart);
//
//        //
//
//        final Session mixSessionExtra = new Session();
//        mixSessionExtra.setId(UUID.randomUUID());
//        mixSessionExtra.setSessionType(SMALL_TRIAL_SESSION_TYPE);
//        mixSessionExtra.setDuration(Duration.ofMinutes(10));
//        mixSessionExtra.setStart(OffsetDateTime.now());
//
//        final Hearing mixMultiHearing = new Hearing();
//        mixMultiHearing.setId(UUID.randomUUID());
//        mixMultiHearing.setCaseNumber(CASE_NUMBER_222);
//        mixMultiHearing.setCaseTitle("mixMultiHearing");
//        mixMultiHearing.setPriority(Priority.Low);
//        mixMultiHearing.setCaseType(fastTrack);
//        mixMultiHearing.setHearingType(trial);
//        mixMultiHearing.setMultiSession(true);
//
//        final HearingPart mixMultiHearingPart = new HearingPart();
//        mixMultiHearingPart.setId(UUID.randomUUID());
//        mixMultiHearingPart.setSessionId(mixSession.getId());
//        mixMultiHearingPart.setHearingId(mixMultiHearing.getId());
//        mixSession.addHearingPart(mixMultiHearingPart);
//
//        final HearingPart mixSingleHearingPartExtra = new HearingPart();
//        mixSingleHearingPartExtra.setId(UUID.randomUUID());
//        mixSingleHearingPartExtra.setSessionId(mixSessionExtra.getId());
//        mixSingleHearingPartExtra.setHearingId(mixMultiHearing.getId());
//        mixSessionExtra.addHearingPart(mixSingleHearingPartExtra);
//
//        Arrays.asList(singleSession, mixSession, multiSession, mixSessionExtra, multiSessionExtra)
//            .forEach(s -> sessionRepository.saveAndFlush(s));
//
//        Arrays.asList(mixMultiHearing, mixSingleHearing, multiHearing, singleHearing)
//            .forEach(h -> hearingRepository.saveAndFlush(h));
//
//        Arrays.asList(singleHearingPart, multiHearingPart, multiHearingPartExtra,
//            mixSingleHearingPart, mixMultiHearingPart, mixSingleHearingPartExtra)
//            .forEach(hp -> hearingPartRepository.saveAndFlush(hp));

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
        startDateCriteria.setKey("startDate");
        startDateCriteria.setOperation(ComparisonOperations.EQUALS);
        startDateCriteria.setValue(NOW_DATE);
        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(startDateCriteria), firstPage);

        assertEquals(sessions.getNumberOfElements(), 1);
    }

    @Test
    public void search_byEndDate_returnSessionWhenEndDateIsBeforeGivenDate() {
        Session tomorrowSession = createSession(null, null, null, null, TOMORROW_DATETIME);
        Session yesterdaySession = createSession(null, null, null, null, YESTERDAY_DATETIME);

        sessionRepository.saveAndFlush(tomorrowSession);
        sessionRepository.saveAndFlush(yesterdaySession);

        val startDateCriteria = new SearchCriteria();
        startDateCriteria.setKey("endDate");
        startDateCriteria.setOperation(ComparisonOperations.EQUALS);
        startDateCriteria.setValue(NOW_DATE);
        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(startDateCriteria), firstPage);

        assertEquals(sessions.getNumberOfElements(), 1);
    }

    @Test
    public void search_bySessionType_returnSessionWithSessionTypeCodeEqualsGivenInSearchCriteria() {
        UUID sessionId = UUID.randomUUID();
        Session sessionWithJudgeKamil = createSession(null, sessionId, null, null, null);
        sessionRepository.saveAndFlush(sessionWithJudgeKamil);

        val sessionTypeCriteria = new SearchCriteria();
        sessionTypeCriteria.setKey("sessionType");
        sessionTypeCriteria.setOperation(ComparisonOperations.IN);
        sessionTypeCriteria.setValue(Arrays.asList(SESSION_TYPE_CODE));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(sessionTypeCriteria), firstPage);

        assertEquals(sessions.getNumberOfElements(), 1);
        assertEquals(sessions.getContent().get(0).getSessionId(), sessionId);
    }

    @Test
    public void search_byRoomId_IN_returnSessionWithRoomIdEqualsGivenInSearchCriteria() {
        Session sessionWithRoomTryton = createSession(null, null, null, ROOM_TRYTON, null);
        Session sessionWithoutRoom = createSession(null, null, null, null, null);

        sessionRepository.saveAndFlush(sessionWithRoomTryton);
        sessionRepository.saveAndFlush(sessionWithoutRoom);

        val roomCriteria = new SearchCriteria();
        roomCriteria.setKey("roomId");
        roomCriteria.setOperation(ComparisonOperations.IN);
        roomCriteria.setValue(Arrays.asList(ROOM_TRYTON_ID));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(roomCriteria), firstPage);

        assertEquals(1, sessions.getNumberOfElements());
        sessions.forEach(s -> {
            assertEquals(s.getRoomId().toString(), ROOM_TRYTON_ID);
            assertEquals(s.getRoomName(), ROOM_NAME);
        });
    }

    @Test
    public void search_byRoomId_IN_OR_NULL_returnSessionWithRoomIdEqualsGivenInSearchCriteriaOrNull() {
        UUID sessionWithRoomId = UUID.randomUUID();
        Session sessionWithRoomTryton = createSession(null, sessionWithRoomId, null, ROOM_TRYTON, null);
        UUID sessionIdWithoutRoom = UUID.randomUUID();
        Session sessionWithoutRoom = createSession(null, sessionIdWithoutRoom, null, null, null);

        sessionRepository.saveAndFlush(sessionWithRoomTryton);
        sessionRepository.saveAndFlush(sessionWithoutRoom);

        val roomCriteria = new SearchCriteria();
        roomCriteria.setKey("roomId");
        roomCriteria.setOperation(ComparisonOperations.IN_OR_NULL);
        roomCriteria.setValue(Arrays.asList(ROOM_TRYTON_ID));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(roomCriteria), firstPage);

        assertEquals(2, sessions.getNumberOfElements());
        val sessionIds = sessions.getContent().stream().map(ssr -> ssr.getSessionId()).collect(Collectors.toList());
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
        personCriteria.setKey("personId");
        personCriteria.setOperation(ComparisonOperations.IN);
        personCriteria.setValue(Arrays.asList(JUDGE_KAMIL_ID.toString()));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(personCriteria), firstPage);

        assertEquals(sessions.getNumberOfElements(), 1);
        sessions.forEach(s -> {
            assertEquals(s.getPersonId(), JUDGE_KAMIL_ID);
            assertEquals(s.getPersonName(), JUDGE_KAMIL_NAME);
        });
    }

    @Test
    public void search_byPersonId_IN_OR_NULL_returnSessionWithPersonIdEqualsGivenInSearchCriteriaOrNull() {
        UUID sessionIdWithJudgeKamil = UUID.randomUUID();
        Session sessionWithJudgeKamil = createSession(null, sessionIdWithJudgeKamil, JUDGE_KAMIL, null, null);
        UUID sessionIdWithoutJudge = UUID.randomUUID();
        Session sessionWithoutJudge = createSession(null, sessionIdWithoutJudge, null, null, null);

        sessionRepository.saveAndFlush(sessionWithJudgeKamil);
        sessionRepository.saveAndFlush(sessionWithoutJudge);

        val personCriteria = new SearchCriteria();
        personCriteria.setKey("personId");
        personCriteria.setOperation(ComparisonOperations.IN_OR_NULL);
        personCriteria.setValue(Arrays.asList(JUDGE_KAMIL_ID.toString()));

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(Arrays.asList(personCriteria), firstPage);

        assertEquals(2, sessions.getNumberOfElements());
    }

        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, CASE_NUMBER_123);
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(1);
//        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

//    @Test
//    public void findAll_withSpecificationOnOperationEquals_shouldReturnEmpty_forNoMatch() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, "NO_MATCH_NUMBER");
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void findAll_withSpecificationOnOperationLike_shouldReturnOneResult_forASingleMatch() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, CASE_NUMBER_123);
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(1);
//        assertThat(responseList.getContent().get(0).getCaseTitle()).isEqualTo("Title 123");
//    }
//
//    @Test
//    public void findAll_withSpecificationOnOperationLike_shouldReturnMultipleResult_forManyMatch() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_TITLE_FIELD, ComparisonOperations.LIKE, "Title");
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(2);
//        assertThat(responseList.getContent().get(0).getCaseTitle()).isEqualTo("Title 123");
//        assertThat(responseList.getContent().get(1).getCaseTitle()).isEqualTo("Title 222");
//    }
//
//    @Test
//    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatch() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD,
//            ComparisonOperations.IN, Arrays.asList(new String[] {SMALL_CLAIMS, FAST_TRACK}));
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(2);
//        assertThat(responseList.getContent().get(0).getCaseTypeCode()).isEqualTo(SMALL_CLAIMS);
//        assertThat(responseList.getContent().get(1).getCaseTypeCode()).isEqualTo(FAST_TRACK);
//    }
//
//    @Test
//    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatchIgnoringMissingOnes() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD,
//            ComparisonOperations.IN, Arrays.asList(new String[] {"missing-in-db-code", SMALL_CLAIMS, FAST_TRACK}));
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(2);
//        assertThat(responseList.getContent().get(0).getCaseTypeCode()).isEqualTo(SMALL_CLAIMS);
//        assertThat(responseList.getContent().get(1).getCaseTypeCode()).isEqualTo(FAST_TRACK);
//    }
//
//    @Test
//    public void findAll_withHearingSpecifications_isListedTrue_shouldReturnOneMatchingResult() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria("listingStatus", ComparisonOperations.EQUALS, "listed");
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(1);
//        assertThat(responseList.getContent().get(0).getIsListed()).isTrue();
//        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
//    }
//
//    @Test
//    public void findAll_withHearingSpecifications_isListedFalse_shouldReturnOneMatchingResult() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria("listingStatus", ComparisonOperations.EQUALS, "unlisted");
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(1);
//        assertThat(responseList.getContent().get(0).getIsListed()).isFalse();
//        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_222);
//    }
//
//    @Test
//    public void findAll_withHearingSpecifications_JudgeId_shouldReturnOneMatchingResult() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        SearchCriteria criteria = new SearchCriteria("reservedJudge.id",
//            ComparisonOperations.IN_OR_NULL, Arrays.asList(new String[] { JUDGE_ID }));
//        criteriaList.add(criteria);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(2);
//        assertThat(responseList.getContent().get(0).getReservedJudgeId().toString()).isEqualTo(JUDGE_ID);
//        assertThat(responseList.getContent().get(1).getReservedJudgeId()).isNull();
//    }
//
//    @Test
//    public void findAll_withEmptyCriteria_ShouldReturnAll() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);
//
//        // Then
//        assertThat(responseList.getContent().size()).isEqualTo(2);
//    }
//
//    @Test
//    public void findAll_withEmptyCriteria_2Pages() {
//        // Given
//        List<SearchCriteria> criteriaList = new ArrayList<>();
//        PageRequest pagable = new PageRequest(0, 1);
//
//        // When
//        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, pagable);
//
//        // Then
//        assertThat(responseList.getTotalPages()).isEqualTo(2);
//        assertThat(responseList.getTotalElements()).isEqualTo(2);
//        assertThat(responseList.getContent().size()).isEqualTo(1);
//    }



