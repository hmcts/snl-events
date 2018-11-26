package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ComparisonOperations;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class HearingServiceTests extends BaseIntegrationTest {
    public static final String CASE_NUMBER_123 = "123";
    public static final String SMALL_CLAIMS = "small-claims";
    public static final String FAST_TRACK = "fast-track";
    public static final String CASE_TYPE_FIELD = "caseType";
    public static final String CASE_TITLE_FIELD = "caseTitle";
    public static final String CASE_NUMBER_FIELD = "caseNumber";
    public static final String CASE_NUMBER_222 = "222";
    public static final String JUDGE_ID = "1143b1ea-1813-4acc-8b08-f37d1db59492";
    private static final UUID HEARING_ID = UUID.randomUUID();

    private final CaseType smallClaims = new CaseType(SMALL_CLAIMS, "SC");
    private final CaseType fastTrack = new CaseType(FAST_TRACK, "FT");
    private final HearingType trial = new HearingType("trial", "Trial");

    private final PageRequest firstPage = new PageRequest(0, 10);

    private Hearing hearingListed;
    private Hearing hearingUnlisted;

    private HearingPart hearingPart;

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
    HearingService hearingService;

    @Autowired
    EntityManager entityManager;

    @Before
    public void setup() {
        // Setup
        final Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setSessionType(new SessionType("small-trial", "ST"));
        session.setDuration(Duration.ofHours(1));
        session.setStart(OffsetDateTime.now());

        final Person judgeLinda = personRepository.getOne(UUID.fromString(JUDGE_ID));

        hearingListed = new Hearing();
        hearingListed.setId(UUID.randomUUID());
        hearingListed.setCaseNumber(CASE_NUMBER_123);
        hearingListed.setCaseTitle("Title 123");
        hearingListed.setPriority(Priority.Low);
        hearingListed.setCaseType(smallClaims);
        hearingListed.setHearingType(trial);
        hearingListed.setCommunicationFacilitator("Sign Language");
        hearingListed.setReservedJudge(judgeLinda);
        hearingListed.setNumberOfSessions(1);
        hearingListed.setMultiSession(false);

        hearingUnlisted = new Hearing();
        hearingUnlisted.setId(UUID.randomUUID());
        hearingUnlisted.setCaseNumber(CASE_NUMBER_222);
        hearingUnlisted.setCaseTitle("Title 222");
        hearingUnlisted.setPriority(Priority.Low);
        hearingUnlisted.setCaseType(fastTrack);
        hearingUnlisted.setHearingType(trial);
        hearingUnlisted.setNumberOfSessions(1);
        hearingUnlisted.setMultiSession(false);

        hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setSessionId(session.getId());
        hearingPart.setHearingId(hearingListed.getId());
        session.addHearingPart(hearingPart);

        final HearingPart hearingPart2 = new HearingPart();
        hearingPart2.setId(UUID.randomUUID());
        hearingPart2.setHearingId(hearingUnlisted.getId());

        hearingListed.addHearingPart(hearingPart);
        hearingUnlisted.addHearingPart(hearingPart2);

        StatusConfig statusListed = new StatusConfig();
        statusListed.setStatus(Status.valueOf("Listed"));

        StatusConfig statusUnlisted = new StatusConfig();
        statusUnlisted.setStatus(Status.valueOf("Unlisted"));

        hearingListed.setStatus(statusListed);
        hearingUnlisted.setStatus(statusUnlisted);

        sessionRepository.saveAndFlush(session);
        hearingRepository.saveAndFlush(hearingListed);
        hearingRepository.saveAndFlush(hearingUnlisted);
    }

    @Test
    public void getHearingForAmend_shouldReturnProperResponse() {
        final Hearing hearing3 = new Hearing();
        hearing3.setId(HEARING_ID);
        hearing3.setCaseNumber("AMENDMENT");
        hearing3.setCaseTitle("FOR AMEND");
        hearing3.setPriority(Priority.Low);
        hearing3.setCaseType(fastTrack);
        hearing3.setHearingType(trial);
        hearing3.setNumberOfSessions(1);
        hearing3.setMultiSession(false);
        val statusConfig = new StatusConfig();
        statusConfig.setStatus(Status.Listed);
        hearing3.setStatus(statusConfig);

        hearingRepository.saveAndFlush(hearing3);

        HearingSearchResponseForAmendment response = hearingService.get(HEARING_ID);

        assertThat(response.getId()).isEqualTo(HEARING_ID);
        assertThat(response.getCaseTitle()).isEqualToIgnoringCase("FOR AMEND");
        assertThat(response.getStatus()).isEqualTo(Status.Listed);
    }

    @Test
    public void findAll_withSpecificationOnOperationEquals_shouldReturnOneResult_forAMatch() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, CASE_NUMBER_123);
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

    @Test
    public void findAll_withSpecificationOnOperationEquals_shouldReturnEmpty_forNoMatch() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, "NO_MATCH_NUMBER");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(0);
    }

    @Test
    public void findAll_withSpecificationOnOperationLike_shouldReturnOneResult_forASingleMatch() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_NUMBER_FIELD, ComparisonOperations.EQUALS, CASE_NUMBER_123);
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getCaseTitle()).isEqualTo("Title 123");
    }

    @Test
    public void findAll_withSpecificationOnOperationLike_shouldReturnMultipleResult_forManyMatch() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_TITLE_FIELD, ComparisonOperations.LIKE, "Title");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(2);
        assertThat(responseList.getContent().get(0).getCaseTitle()).isEqualTo("Title 123");
        assertThat(responseList.getContent().get(1).getCaseTitle()).isEqualTo("Title 222");
    }

    @Test
    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatch() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD,
            ComparisonOperations.IN, Arrays.asList(new String[] {SMALL_CLAIMS, FAST_TRACK}));
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(2);
        assertThat(responseList.getContent().get(0).getCaseTypeCode()).isEqualTo(SMALL_CLAIMS);
        assertThat(responseList.getContent().get(1).getCaseTypeCode()).isEqualTo(FAST_TRACK);
    }

    @Test
    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatchIgnoringMissingOnes() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD,
            ComparisonOperations.IN, Arrays.asList(new String[] {"missing-in-db-code", SMALL_CLAIMS, FAST_TRACK}));
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(2);
        assertThat(responseList.getContent().get(0).getCaseTypeCode()).isEqualTo(SMALL_CLAIMS);
        assertThat(responseList.getContent().get(1).getCaseTypeCode()).isEqualTo(FAST_TRACK);
    }

    @Test
    public void findAll_withHearingSpecifications_isListedTrue_shouldReturnOneMatchingResult() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria("status.status", ComparisonOperations.EQUALS, "Listed");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getStatus().toString()).isEqualTo("Listed");
        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

    @Test
    public void findAll_withHearingSpecifications_isListedFalse_shouldReturnOneMatchingResult() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria("status.status", ComparisonOperations.EQUALS, "Unlisted");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getStatus().toString()).isEqualTo("Unlisted");
        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_222);
    }

    @Test
    public void findAll_withHearingSpecifications_JudgeId_shouldReturnOneMatchingResult() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria("reservedJudge.id",
            ComparisonOperations.IN_OR_NULL, Arrays.asList(new String[] { JUDGE_ID }));
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(2);
        assertThat(responseList.getContent().get(0).getReservedJudgeId().toString()).isEqualTo(JUDGE_ID);
        assertThat(responseList.getContent().get(1).getReservedJudgeId()).isNull();
    }

    @Test
    public void findAll_withEmptyCriteria_ShouldReturnAll() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(2);
    }

    @Test
    public void findAll_withEmptyCriteria_2Pages() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        PageRequest pagable = new PageRequest(0, 1);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, pagable);

        // Then
        assertThat(responseList.getTotalPages()).isEqualTo(2);
        assertThat(responseList.getTotalElements()).isEqualTo(2);
        assertThat(responseList.getContent().size()).isEqualTo(1);
    }

    @Test
    public void getHearingsForListing_twoPages() {
        // Given
        Hearing.HearingBuilder unlistedHearingBuilder = hearingUnlisted.toBuilder();

        for (int i = 0 ; i < 10 ; i++) {
            Hearing tempUnlistedHearing = unlistedHearingBuilder
                .id(UUID.randomUUID())
                .hearingParts(Arrays.asList(hearingPart.toBuilder().id(UUID.randomUUID()).build()))
                .build();
            hearingRepository.saveAndFlush(tempUnlistedHearing);
        }
        // When
        final Page<HearingForListingResponse> firstPage = hearingService.getHearingsForListing(Optional.of(0),
            Optional.of(10),
            Optional.of("case_number"),
            Optional.of("asc"));

        final Page<HearingForListingResponse> secondPage = hearingService.getHearingsForListing(Optional.of(1),
            Optional.of(10),
            Optional.of("case_number"),
            Optional.of("asc"));

        // Then
        assertThat(firstPage.getTotalElements()).isEqualTo(11);
        assertThat(firstPage.getContent().size()).isEqualTo(10);

        assertThat(secondPage.getTotalElements()).isEqualTo(11);
        assertThat(secondPage.getContent().size()).isEqualTo(1);
    }
}


