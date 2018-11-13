package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusConfigService;
import uk.gov.hmcts.reform.sandl.snlevents.service.StatusServiceManager;

@org.springframework.boot.test.context.TestConfiguration
public class StatusesTestConfiguration {

    private static StatusesMock mockHolder = new StatusesMock();

    @Bean
    public StatusServiceManager statusServiceManager() {
        return mockHolder.statusServiceManager;
    }

    @Bean
    public StatusConfigService statusConfigService() {
        return mockHolder.statusConfigService;
    }

    @Bean
    public StatusConfigRepository statusConfigRepository() {
        return mockHolder.statusConfigRepository;
    }
}
