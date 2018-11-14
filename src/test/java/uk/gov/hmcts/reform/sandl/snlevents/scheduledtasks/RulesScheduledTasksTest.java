package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReloadRulesService;

import java.io.IOException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class RulesScheduledTasksTest {

    @InjectMocks
    private RulesScheduledTasks rulesScheduledTasks;

    @Mock
    private ReloadRulesService reloadRulesService;

    @Test
    public void all_methods_should_be_run_in_services() throws IOException {
        doNothing().when(reloadRulesService).reloadIfNeeded();
        doNothing().when(reloadRulesService).reloadDateAndTimeIfNeeded();

        rulesScheduledTasks.reloadRulesFactsFromDb();
        rulesScheduledTasks.setDateAndTime();

        verify(reloadRulesService, times(1)).reloadIfNeeded();
        verify(reloadRulesService, times(1)).reloadDateAndTimeIfNeeded();
    }
}
