package uk.gov.hmcts.reform.sandl.snlevents.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.common.OurMockMvc;

@Configuration
public class TestConfiguration {

    @Bean
    @Autowired
    public OurMockMvc ourMockMvc(
        MockMvc mockMvc,
        ObjectMapper objectMapper
    ) {
        return new OurMockMvc(mockMvc, objectMapper);
    }
}
