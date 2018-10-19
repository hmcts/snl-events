package uk.gov.hmcts.reform.sandl.snlevents.repositories;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.models.BaseIntegrationModelTest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.HearingSpecifications;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.SearchCriteriaSpecifications;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class HearingRepositoryTests extends BaseIntegrationModelTest {

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
        hearing.setReservedJudgeId(judgeLinda.getId());

        final Hearing hearing2 = new Hearing();
        hearing2.setId(UUID.randomUUID());
        hearing2.setCaseNumber(CASE_NUMBER_222);
        hearing2.setCaseTitle("Title 222");
        hearing2.setPriority(Priority.Low);
        hearing2.setCaseType(smallClaims);
        hearing2.setHearingType(trial);

        final HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setSessionId(session.getId());
        hearingPart.setHearingId(hearing.getId());


        final HearingPart hearingPart2 = new HearingPart();
        hearingPart2.setId(UUID.randomUUID());
        hearingPart2.setHearingId(hearing2.getId());

        sessionRepository.save(session);
        hearingRepository.save(hearing);
        hearingRepository.save(hearing2);
        hearingPartRepository.saveAndFlush(hearingPart);
        hearingPartRepository.saveAndFlush(hearingPart2);
    }

    @Test
    public void findAll_withSpecificationOnOperationEquals_shouldReturnOneResult_forAMatch() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.equals(CASE_NUMBER_FIELD, CASE_NUMBER_123));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(1);
        assertThat(hearings.get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

    @Test
    public void findAll_withSpecificationOnOperationEquals_shouldReturnEmpty_forNoMatch() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.equals(CASE_NUMBER_FIELD, "NO_MATCH_NUMBER"));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(0);
    }

    @Test
    public void findAll_withSpecificationOnOperationLike_shouldReturnOneResult_forASingleMatch() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.like(CASE_TITLE_FIELD, CASE_NUMBER_123));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(1);
        assertThat(hearings.get(0).getCaseTitle()).isEqualTo("Title 123");
    }

    @Test
    public void findAll_withSpecificationOnOperationLike_shouldReturnMultipleResult_forManyMatch() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.like(CASE_TITLE_FIELD, "Title"));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(2);
        assertThat(hearings.get(0).getCaseTitle()).isEqualTo("Title 123");
        assertThat(hearings.get(1).getCaseTitle()).isEqualTo("Title 222");
    }

    @Test
    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatch() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.in(CASE_TYPE_FIELD,
            new CaseType[] {smallClaims, fastTrack})
        );

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(2);
        // Update below when ordering is added
//        assertThat(hearings.get(0).getCaseType().getCode()).isEqualTo(SMALL_CLAIMS);
//        assertThat(hearings.get(1).getCaseType().getCode()).isEqualTo(FAST_TRACK);
    }

    @Test
    public void findAll_withSpecificationOnOperationIn_shouldReturnMultipleResult_forManyMatchIgnoringMissingOnes() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(SearchCriteriaSpecifications.in(CASE_TYPE_FIELD,
            new CaseType[] {new CaseType("missing-in-db-code", ""), smallClaims, fastTrack})
        );

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(2);
        // Update below when ordering is added
//        assertThat(hearings.get(0).getCaseType().getCode()).isEqualTo(SMALL_CLAIMS);
//        assertThat(hearings.get(1).getCaseType().getCode()).isEqualTo(FAST_TRACK);
    }

    @Test
    public void findAll_withHearingSpecifications_isListedTrue_shouldReturnOneMatchingResult() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(HearingSpecifications.isListed(true));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(1);
        assertThat(hearings.get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_123);
    }

    @Test
    public void findAll_withHearingSpecifications_isListedFalse_shouldReturnOneMatchingResult() {
        // Given
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(HearingSpecifications.isListed(false));

        Specification<Hearing> specification = Specifications.where(specs.get(0));
        // When
        final List<Hearing> hearings = hearingRepository.findAll(specification);

        // Then
        assertThat(hearings.size()).isEqualTo(1);
        assertThat(hearings.get(0).getCaseNumber()).isEqualTo(CASE_NUMBER_222);
    }

}
