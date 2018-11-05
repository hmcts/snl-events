package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StatusConfigServiceTest {
    @InjectMocks
    StatusConfigService statusConfigService;

    @Mock
    StatusConfigRepository statusConfigRepository;

    @Test
    public void getStatuses_shouldReturnListOfStatuses() {
        val dbStatuses = getDefaultDbStatuses();
        when(statusConfigRepository.findAll()).thenReturn(dbStatuses);
        val returnedStatuses = statusConfigService.getStatuses();

        assertThat(returnedStatuses).isEqualTo(dbStatuses);
    }

    @Test
    public void get_shouldReturnProperStatusConfig() {
        val dbStatuses = getDefaultDbStatuses();
        when(statusConfigRepository.findAll()).thenReturn(dbStatuses);

        val returnedStatus = statusConfigService.get(Status.Unlisted);

        assertThat(returnedStatus.getStatus()).isEqualTo(Status.Unlisted);
    }

    @Test(expected = NoSuchElementException.class)
    public void get_shouldThrowNoSuchElementExceptionForWrongDbState() {
        when(statusConfigRepository.findAll()).thenReturn(new ArrayList<>());

        val returnedStatus = statusConfigService.get(Status.Listed);
    }

    private List<StatusConfig> getDefaultDbStatuses() {
        return Arrays.asList(
            new StatusConfig(Status.Listed, false, true, true),
            new StatusConfig(Status.Unlisted, true, false, false)
        );
    }
}
