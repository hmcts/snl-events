package uk.gov.hmcts.reform.sandl.snlevents.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationConfig;
import uk.gov.hmcts.reform.sandl.snlevents.security.ServiceAuthenticationEntryPoint;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @Bean
    @Autowired
    public EventsMockMvc eventsMockMvc(
        MockMvc mockMvc,
        ObjectMapper objectMapper
    ) {
        return new EventsMockMvc(mockMvc, objectMapper);
    }

    @Bean
    public S2SAuthenticationConfig s2SAuthenticationConfig() {
        return new S2SAuthenticationConfig();
    }

    @Bean
    public ServiceAuthenticationEntryPoint unauthorizedHandler() {
        return new ServiceAuthenticationEntryPoint();
    };
}
