package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class S2SAuthenticationServiceTest {

    private static final String SECRET_EVENTS = "SecretE";
    private static final String SECRET_RULES = "SecrerR";
    private static final int DEFAULT_EXPIRY = 3000;
    private static final String SERVICE_NAME_SNL_API = "snl-api";
    private S2SAuthenticationConfig config;
    private S2SAuthenticationService s2SAuthenticationService;

    @Before
    public void setup() {
        val s2sConfig = Mockito.mock(S2SAuthenticationConfig.class);
        when(s2sConfig.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, DEFAULT_EXPIRY));
        when(s2sConfig.getRules())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_RULES, 1001));
        config = s2sConfig;

        s2SAuthenticationService = new S2SAuthenticationService(config);
    }

    @Test
    public void validateToken_returnsFalse_forMissingToken() {
        final String token = "";
        boolean result = this.s2SAuthenticationService.validateToken(token);
        assertThat(result).isFalse();
    }

    @Test
    public void createRulesAuthenticationHeader_returnsHeaderWithNewlyCreatedToken() {
        HttpHeaders result = this.s2SAuthenticationService.createRulesAuthenticationHeader();

        assertThat(result.containsKey(S2SAuthenticationService.HEADER_NAME)).isTrue();
        assertThat(result.getFirst(S2SAuthenticationService.HEADER_NAME))
            .contains(S2SAuthenticationService.HEADER_CONTENT_PREFIX);
    }

    @Test
    public void validateToken_returnsTrue_forValidSecretAndTimeout() {
        when(config.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, DEFAULT_EXPIRY));

        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_EVENTS, DEFAULT_EXPIRY, SERVICE_NAME_SNL_API)
            .createToken();

        boolean result = this.s2SAuthenticationService.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    public void validateToken_returnsFalse_forInvalidServiceName() {
        when(config.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, DEFAULT_EXPIRY));

        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_EVENTS, DEFAULT_EXPIRY, "snl-INVALID")
            .createToken();
        boolean result = this.s2SAuthenticationService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    public void validateToken_returnsFalse_forValidSecretAndWrongTimeout() {
        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_EVENTS, DEFAULT_EXPIRY, SERVICE_NAME_SNL_API)
            .createToken();

        when(config.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, DEFAULT_EXPIRY - 1));
        boolean result = this.s2SAuthenticationService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test(expected = SignatureException.class)
    public void validateToken_throwsException_forInValidSecretAndWrongTimeout() {
        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_EVENTS, DEFAULT_EXPIRY, SERVICE_NAME_SNL_API)
            .createToken();

        when(config.getEvents())
            .thenReturn(new S2SAuthenticationConfig
                .JwtCredentials(SECRET_EVENTS + "A", DEFAULT_EXPIRY - 1));
        this.s2SAuthenticationService.validateToken(token);
    }

    @Test(expected = SignatureException.class)
    public void validateToken_throwsException_forInValidSecretAndGoodTimeout() {
        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_EVENTS, DEFAULT_EXPIRY, SERVICE_NAME_SNL_API)
            .createToken();

        when(config.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS + "A", DEFAULT_EXPIRY));
        this.s2SAuthenticationService.validateToken(token);
    }

    @Test
    public void tokenCreator_createToken_forRules_containsProperFields() {
        final String token = s2SAuthenticationService.new TokenCreator(
            SECRET_RULES, DEFAULT_EXPIRY, "snl-events")
            .createToken();

        Claims claims = new DefaultClaims();
        boolean exceptionThrown = false;
        try {
            claims = Jwts.parser()
                .setSigningKey(config.getRules().getJwtSecret())
                .parseClaimsJws(token)
                .getBody();
        } catch (SignatureException ex) {
            exceptionThrown = true;
        }
        assertThat(exceptionThrown).isFalse();

        final String serviceName = (String) claims.get("service");
        assertThat("snl-events").isEqualTo(serviceName);

        long millisDifference = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(config.getEvents().getJwtExpirationInMs()).isEqualTo(millisDifference);
    }

}
