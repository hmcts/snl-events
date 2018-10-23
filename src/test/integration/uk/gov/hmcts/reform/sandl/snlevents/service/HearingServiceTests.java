package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.ComparisonOperations;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.SearchCriteria;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

    private final CaseType smallClaims = new CaseType(SMALL_CLAIMS, "SC");
    private final CaseType fastTrack = new CaseType(FAST_TRACK, "FT");
    private final HearingType trial = new HearingType("trial", "Trial");

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
    HearingService hearingService;

    @Before
    public void setup() {
        // Setup
        final Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setSessionType(new SessionType("small-trial", "ST"));
        session.setDuration(Duration.ofHours(1));
        session.setStart(OffsetDateTime.now());

        final Person judgeLinda = personRepository.getOne(UUID.fromString("1143b1ea-1813-4acc-8b08-f37d1db59492"));

        final Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        hearing.setCaseNumber(CASE_NUMBER_123);
        hearing.setCaseTitle("Title 123");
        hearing.setPriority(Priority.Low);
        hearing.setCaseType(smallClaims);
        hearing.setHearingType(trial);
        hearing.setCommunicationFacilitator("Sign Language");
        hearing.setReservedJudge(judgeLinda);

        final Hearing hearing2 = new Hearing();
        hearing2.setId(UUID.randomUUID());
        hearing2.setCaseNumber(CASE_NUMBER_222);
        hearing2.setCaseTitle("Title 222");
        hearing2.setPriority(Priority.Low);
        hearing2.setCaseType(fastTrack);
        hearing2.setHearingType(trial);

        final HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setSessionId(session.getId());
        hearingPart.setHearingId(hearing.getId());
        session.addHearingPart(hearingPart);

        final HearingPart hearingPart2 = new HearingPart();
        hearingPart2.setId(UUID.randomUUID());
        hearingPart2.setHearingId(hearing2.getId());

        hearing.addHearingPart(hearingPart);
        hearing2.addHearingPart(hearingPart2);

        sessionRepository.saveAndFlush(session);
        hearingRepository.saveAndFlush(hearing);
        hearingRepository.saveAndFlush(hearing2);
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
        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD, ComparisonOperations.IN, Arrays.asList(new String[] {SMALL_CLAIMS, FAST_TRACK}));
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
        SearchCriteria criteria = new SearchCriteria(CASE_TYPE_FIELD, ComparisonOperations.IN, Arrays.asList(new String[] {"missing-in-db-code", SMALL_CLAIMS, FAST_TRACK}));
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
        SearchCriteria criteria = new SearchCriteria("listingStatus", ComparisonOperations.EQUALS, "listed");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

    @Test
    public void findAll_withHearingSpecifications_isListedFalse_shouldReturnOneMatchingResult() {
        // Given
        List<SearchCriteria> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria("listingStatus", ComparisonOperations.EQUALS, "unlisted");
        criteriaList.add(criteria);

        // When
        final Page<HearingSearchResponse> responseList = hearingService.search(criteriaList, firstPage);

        // Then
        assertThat(responseList.getContent().size()).isEqualTo(1);
        assertThat(responseList.getContent().get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_222);
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
}
