package uk.gov.hmcts.reform.sandl.snlevents.repositories;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.models.BaseIntegrationModelTest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ProblemRepositoryTests extends BaseIntegrationModelTest  {
    private static final String WARNING = "Warning";
    private static final String URGENT = "Urgent";
    private static final String CRITICAL = "Critical";

    @Autowired
    ProblemRepository problemRepository;

    @Test
    public void addSession_shouldSetCorrespondentRelationInSession() {
        Problem nowWarning = generateProblem(OffsetDateTime.now(), WARNING);
        Problem threeHourAgoCritical = generateProblem(OffsetDateTime.now().minusHours(3), CRITICAL);
        Problem hourAgoUrgent = generateProblem(OffsetDateTime.now().minusHours(1), URGENT);
        Problem fiveMinutesAgoWarning = generateProblem(OffsetDateTime.now().minusMinutes(5), WARNING);
        Problem nowWithoutCreatedAtAndDifferentSeverity = generateProblem(OffsetDateTime.now().minusMinutes(5), WARNING);

        List<Problem> problems = new ArrayList<>();
        problems.add(nowWarning);
        problems.add(threeHourAgoCritical);
        problems.add(hourAgoUrgent);
        problems.add(fiveMinutesAgoWarning);
        problems.add(nowWithoutCreatedAtAndDifferentSeverity);

        problemRepository.save(problems);

        List<Problem> sortedProblems = problemRepository.getAllSortedBySeverityAndCreatedAt();

        List<Problem> expectedOrder = Arrays.asList(
            threeHourAgoCritical,
            hourAgoUrgent,
            nowWarning,
            fiveMinutesAgoWarning,
            nowWithoutCreatedAtAndDifferentSeverity
        );

        assertThat(sortedProblems).isEqualTo(expectedOrder);
    }

    private Problem generateProblem(OffsetDateTime createdAt, String severity) {
        Problem problem = new Problem();
        problem.setId(UUID.randomUUID().toString());
        problem.setCreatedAt(createdAt);
        problem.setSeverity(severity);

        return  problem;
    }
}
