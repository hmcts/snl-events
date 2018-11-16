package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeansConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
