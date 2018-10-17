package uk.gov.hmcts.reform.sandl.snlevents.repositories;

import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ProblemReference;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;
import uk.gov.hmcts.reform.sandl.snlevents.models.BaseIntegrationModelTest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.UserTransactionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ProblemQueries;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ProblemRepositoryTests extends BaseIntegrationModelTest  {
    private static final String WARNING = "Warning";
    private static final String URGENT = "Urgent";
    private static final String CRITICAL = "Critical";

    @Autowired
    ProblemRepository problemRepository;

    @Autowired
    UserTransactionRepository userTransactionRepository;

    @Test
    public void getProblems_queryIsDefined() {
        assertThat(ProblemQueries.GET_PROBLEMS).isNotEmpty();
    }

    @Test
    @Deprecated
    public void addSession_shouldSetCorrespondentRelationInSession() {
        // note do not change to random UUID because this may change the order
        // and consequently the test to fail
        Problem nowWarning = generateProblem("11", OffsetDateTime.now(), WARNING);
        Problem threeHourAgoCritical = generateProblem("22", OffsetDateTime.now().minusHours(3), CRITICAL);
        Problem hourAgoUrgent = generateProblem("33", OffsetDateTime.now().minusHours(1), URGENT);
        val sameTime = OffsetDateTime.now().minusMinutes(5); // so we do not have a millisecond difference
        Problem fiveMinutesAgoWarning = generateProblem("55", sameTime, WARNING);
        Problem withoutCreatedAtAndDifferentSeverity = generateProblem("44", sameTime, WARNING);

        List<Problem> problems = new ArrayList<>();
        problems.add(nowWarning);
        problems.add(threeHourAgoCritical);
        problems.add(hourAgoUrgent);
        problems.add(fiveMinutesAgoWarning);
        problems.add(withoutCreatedAtAndDifferentSeverity);

        problemRepository.save(problems);

        List<Problem> sortedProblems = problemRepository.getAllSortedBySeverityAndCreatedAt();

        List<Problem> expectedOrder = Arrays.asList(
            threeHourAgoCritical,
            hourAgoUrgent,
            nowWarning,
            fiveMinutesAgoWarning,
            withoutCreatedAtAndDifferentSeverity
        );

        assertThat(sortedProblems).isEqualTo(expectedOrder);
    }

    @Test
    public void addSession_shouldSetCorrespondentRelationInSessionAndReturnPagable() {
        // note do not change to random UUID because this may change the order
        // and consequently the test to fail
        Problem nowWarning = generateProblem("11", OffsetDateTime.now(), WARNING);
        Problem threeHourAgoCritical = generateProblem("22", OffsetDateTime.now().minusHours(3), CRITICAL);
        Problem hourAgoUrgent = generateProblem("33", OffsetDateTime.now().minusHours(1), URGENT);
        val sameTime = OffsetDateTime.now().minusMinutes(5); // so we do not have a millisecond difference
        Problem fiveMinutesAgoWarning = generateProblem("55", sameTime, WARNING);
        Problem withoutCreatedAtAndDifferentSeverity = generateProblem("44", sameTime, WARNING);

        List<Problem> problems = new ArrayList<>();
        problems.add(nowWarning);
        problems.add(threeHourAgoCritical);
        problems.add(hourAgoUrgent);
        problems.add(fiveMinutesAgoWarning);
        problems.add(withoutCreatedAtAndDifferentSeverity);

        problemRepository.save(problems);

        Page<Problem> sortedProblemsPage = problemRepository.getAllSortedBySeverityAndCreatedAt(new PageRequest(0, 20));

        List<Problem> expectedOrder = Arrays.asList(
            threeHourAgoCritical,
            hourAgoUrgent,
            nowWarning,
            fiveMinutesAgoWarning,
            withoutCreatedAtAndDifferentSeverity
        );

        assertThat(sortedProblemsPage.getContent()).isEqualTo(expectedOrder);
    }

    @Test
    public void getProblemsByReferenceEntityId_shouldReturnProblemsForReferencedEntity() {
        Problem nowWarning = generateProblem(OffsetDateTime.now(), WARNING);
        nowWarning.setReferences(new ArrayList<>());
        nowWarning.getReferences().add(generateProblemReference("one"));
        nowWarning.getReferences().add(generateProblemReference("two"));

        String problemId = UUID.randomUUID().toString();

        Problem threeHourAgoCritical = generateProblem(problemId, OffsetDateTime.now().minusHours(3), CRITICAL);
        threeHourAgoCritical.setReferences(new ArrayList<>());
        threeHourAgoCritical.getReferences().add(generateProblemReference("three"));
        String entityId = UUID.randomUUID().toString();
        threeHourAgoCritical.getReferences().add(generateProblemReference(entityId, "here is my entity id"));
        threeHourAgoCritical.getReferences().add(generateProblemReference("four"));

        Problem hourAgoUrgent = generateProblem(OffsetDateTime.now().minusHours(1), URGENT);
        hourAgoUrgent.setReferences(new ArrayList<>());
        hourAgoUrgent.getReferences().add(generateProblemReference("five"));

        List<Problem> problems = new ArrayList<>();
        problems.add(nowWarning);
        problems.add(threeHourAgoCritical);
        problems.add(hourAgoUrgent);

        problemRepository.save(problems);

        List<Problem> problemsByRef = problemRepository.getProblemsByReferenceEntityId(entityId);

        assertThat(problemsByRef.size()).isEqualTo(1);
        assertThat(problemsByRef.get(0).getId()).isEqualTo(problemId);
        assertThat(problemsByRef.get(0).getReferences().stream()
            .map(r -> r.getEntityId()).collect(Collectors.toList()))
            .contains(entityId);
    }

    @Test
    public void getProblemsByUserTransactionId_shouldReturnProblemsForUserTransaction() {
        UUID userTransactionId = UUID.randomUUID();
        UserTransaction userTransaction = new UserTransaction(userTransactionId,
            UserTransactionStatus.INPROGRESS, UserTransactionRulesProcessingStatus.COMPLETE);
        userTransactionRepository.save(userTransaction);

        String problemId = UUID.randomUUID().toString();
        Problem nowWarning = generateProblem(OffsetDateTime.now(), WARNING);
        Problem threeHourAgoCritical = generateProblem(problemId,
            userTransactionId,
            OffsetDateTime.now().minusHours(3), CRITICAL);
        Problem hourAgoUrgent = generateProblem(OffsetDateTime.now().minusHours(1), URGENT);

        List<Problem> problems = new ArrayList<>();
        problems.add(nowWarning);
        problems.add(threeHourAgoCritical);
        problems.add(hourAgoUrgent);

        problemRepository.save(problems);

        List<Problem> problemsByUTranId = problemRepository.getProblemsByUserTransactionId(userTransactionId);

        assertThat(problemsByUTranId.size()).isEqualTo(1);
        assertThat(problemsByUTranId.get(0).getId()).isEqualTo(problemId);
        assertThat(problemsByUTranId.get(0).getUserTransactionId()).isEqualTo(userTransactionId);
    }

    private Problem generateProblem(OffsetDateTime createdAt, String severity) {
        return generateProblem(UUID.randomUUID().toString(), createdAt, severity);
    }

    private Problem generateProblem(String problemId, OffsetDateTime createdAt, String severity) {
        return generateProblem(problemId, null, createdAt, severity);
    }

    private Problem generateProblem(String problemId, UUID userTransactionId,
                                    OffsetDateTime createdAt, String severity) {
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setCreatedAt(createdAt);
        problem.setSeverity(severity);
        problem.setUserTransactionId(userTransactionId);

        return  problem;
    }

    private ProblemReference generateProblemReference(String description) {
        return generateProblemReference(UUID.randomUUID().toString(), description);
    }

    private ProblemReference generateProblemReference(String entityId, String description) {
        ProblemReference problemReference = new ProblemReference();

        problemReference.setEntity("Sample");
        problemReference.setEntityId(entityId);
        problemReference.setDescription(description);

        return problemReference;
    }
}
