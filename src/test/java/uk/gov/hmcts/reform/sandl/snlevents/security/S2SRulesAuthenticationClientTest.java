package uk.gov.hmcts.reform.sandl.snlevents.security;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class S2SRulesAuthenticationClientTest {
    private static final String SECRET_RULES = "SecretR";
    private static final int DEFAULT_EXPIRY = 5000;

    private S2SAuthenticationConfig s2SAuthenticationConfig;
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Before
    public void setup() {
        s2SAuthenticationConfig = Mockito.mock(S2SAuthenticationConfig.class);
        when(s2SAuthenticationConfig.getRules())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_RULES, DEFAULT_EXPIRY));

        s2SRulesAuthenticationClient = new S2SRulesAuthenticationClient(s2SAuthenticationConfig);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createRulesAuthenticationHeader_createsValidHeader_forValidConfig() {
        val headers = s2SRulesAuthenticationClient.createRulesAuthenticationHeader();
        assertThat(headers.containsKey((TokenService.HEADER_NAME)));
        assertThat(headers.get(TokenService.HEADER_NAME).get(0)).startsWith(TokenService.HEADER_CONTENT_PREFIX);
    }
}
