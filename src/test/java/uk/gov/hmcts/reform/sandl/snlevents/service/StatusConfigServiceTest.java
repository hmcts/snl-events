package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.ArrayList;
import java.util.List;

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
        when(statusConfigRepository.findAll()).thenReturn(createStatuses());
        statusConfigService.getStatusConfigs();
        statusConfigService.getStatusConfigs();

        verify(statusConfigRepository, times(1)).findAll();
    }

    private List<StatusConfig> createStatuses() {
        List<StatusConfig> statusConfigs = new ArrayList<>();
        statusConfigs.add(new StatusConfig());

        return statusConfigs;
    }
}
