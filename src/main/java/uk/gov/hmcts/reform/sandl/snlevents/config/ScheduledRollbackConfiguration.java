package uk.gov.hmcts.reform.sandl.snlevents.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("scheduler.auto-rollback")
@Validated
@Getter
@Setter
public class ScheduledRollbackConfiguration {
    private int timeoutIntervalInMinutes;
    private boolean enabled;
}
