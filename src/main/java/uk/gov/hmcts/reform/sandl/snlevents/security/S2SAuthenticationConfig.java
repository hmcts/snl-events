package uk.gov.hmcts.reform.sandl.snlevents.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "management.security")
@EnableConfigurationProperties
@NoArgsConstructor
@Getter
@Setter
public class S2SAuthenticationConfig {
    private JwtCredentials events;
    private JwtCredentials rules;
    private boolean enabled;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JwtCredentials {
        String jwtSecret;
        long jwtExpirationInMs;
    }
}
