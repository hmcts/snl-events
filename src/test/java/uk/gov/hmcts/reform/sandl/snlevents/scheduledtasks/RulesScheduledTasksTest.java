package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReloadRulesService;

import java.io.IOException;
import java.net.ConnectException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
        rulesScheduledTasks.setRulesDateAndTime();

        verify(reloadRulesService, times(1)).reloadIfNeeded();
        verify(reloadRulesService, times(1)).reloadDateAndTimeIfNeeded();
    }

    @Test
    public void setRulesdo_not_throw_ex_when_connection_errors() throws IOException {
        doThrow(new ResourceAccessException("ex", new ConnectException()))
            .when(reloadRulesService).reloadIfNeeded();
        doThrow(new ResourceAccessException("ex", new ConnectException()))
            .when(reloadRulesService).reloadDateAndTimeIfNeeded();

        rulesScheduledTasks.reloadRulesFactsFromDb();
        rulesScheduledTasks.setRulesDateAndTime();
    }

    @Test(expected = ResourceAccessException.class)
    public void reloadRulesFactsFromDb_throw_non_connection_errors() throws IOException {
        doThrow(new ResourceAccessException("ex"))
            .when(reloadRulesService).reloadIfNeeded();

        rulesScheduledTasks.reloadRulesFactsFromDb();
    }

    @Test(expected = ResourceAccessException.class)
    public void setRulesDateAndTime_throw_non_connection_errors() throws IOException {
        doThrow(new ResourceAccessException("ex"))
            .when(reloadRulesService).reloadDateAndTimeIfNeeded();

        rulesScheduledTasks.setRulesDateAndTime();
    }
}
