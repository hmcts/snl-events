package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StatusConfigServiceTest {

    @InjectMocks
    private StatusConfigService statusConfigService;

    @Mock
    private StatusConfigRepository statusConfigRepository;

    @Test
    public void shouldCallRepositoryOnlyOnce() {
        when(statusConfigRepository.findAll()).thenReturn(StatusesMock.createSampleStatuses());
        statusConfigService.getStatusConfigs();
        statusConfigService.getStatusConfigs();

        verify(statusConfigRepository, times(1)).findAll();
    }

    @Test(expected = SnlRuntimeException.class)
    public void getStatusConfig_shouldThrowExceptionIfNoStatusFound() {
        // no setup, just a call
        statusConfigService.getStatusConfig(Status.Listed);
    }

    @Test
    public void getStatusConfig_calledWithKnownStatus_shouldReturnProperStatusConfig() {
        // no setup, just a call
        when(statusConfigRepository.findAll()).thenReturn(StatusesMock.createSampleStatuses());
        final StatusConfig statusConfigReturned = statusConfigService.getStatusConfig(Status.Listed);

        assertThat(statusConfigReturned.getStatus()).isEqualTo(Status.Listed);
    }

}
