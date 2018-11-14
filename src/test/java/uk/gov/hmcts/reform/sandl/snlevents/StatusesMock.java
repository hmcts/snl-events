package uk.gov.hmcts.reform.sandl.snlevents;

import org.mockito.Mockito;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class StatusesMock {
    public StatusServiceManager statusServiceManager;
    public StatusConfigService statusConfigService;
    public StatusConfigRepository statusConfigRepository;

    public StatusesMock() {
        this.statusConfigRepository = Mockito.mock(StatusConfigRepository.class);
        when(statusConfigRepository.findAll()).thenReturn(StatusesMock.createSampleStatuses());

        this.statusServiceManager = new StatusServiceManager();
        this.statusConfigService = new StatusConfigService(statusConfigRepository);
    }

    public static List<StatusConfig> createSampleStatuses() {
        List<StatusConfig> statusConfigs = new ArrayList<>();
        statusConfigs.add(new StatusConfig(Status.Listed, true, true, true));
        statusConfigs.add(new StatusConfig(Status.Unlisted, true, false,false));
        return statusConfigs;
    }
}
