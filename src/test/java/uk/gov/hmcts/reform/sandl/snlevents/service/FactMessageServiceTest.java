package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FactMessageServiceTest {
    @InjectMocks
    private FactMessageService factMessageService;

    @Mock
    private ProblemService problemService;

    @Test
    public void handle_savesProblemToProblemService() throws IOException {
        when(problemService.problemCreateToDb(any())).thenReturn(createProblem());
        factMessageService.handle(UUID.randomUUID(), createFactMsgJson());
        verify(problemService, times(1)).save(any(Problem.class));
    }

    private Problem createProblem() {
        return new Problem();
    }

    private String createFactMsgJson() {
        return "["
            + "{"
            + "\"type\": \"Problem\","
            + "\"newFact\":"
            + "{"
            + "\"id\":\"id\""
            + "}"
            + "}"
            + "]";
    }
}
