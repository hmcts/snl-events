package uk.gov.hmcts.reform.sandl.snlevents.fakerules.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.util.Date;

import static uk.gov.hmcts.reform.sandl.snlevents.fakerules.security.S2SJwtAuthenticationFilterIntegrationTest.Helper.HEADER_NAME;

public class S2SJwtAuthenticationFilterIntegrationTest extends BaseIntegrationTestWithFakeRules {

    @LocalServerPort
    private int port;

    @Autowired
    S2SRulesAuthenticationClient s2sAuthService;

    private Helper helper = new Helper();

    @Test
    public void getRoom_orAnyOther_shouldPass_WithValidS2SJwtToken() {

        HttpStatus expectedStatusCode = HttpStatus.OK;

        HttpHeaders headers = helper.createEventsAuthenticationHeader();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        TestRestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            getTestUrl(), HttpMethod.GET, entity, String.class);

        Assert.assertEquals(expectedStatusCode, response.getStatusCode());
    }

    @Test
    public void getRoom_orOtherEndpointsNeedingJwtToken_shouldReturnUnauthorized_ForInvalidS2SJwtToken() {

        HttpStatus expectedStatusCode = HttpStatus.UNAUTHORIZED;

        HttpHeaders headers = helper.createEventsAuthenticationHeader();
        headers.set(HEADER_NAME, "INVALID");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        TestRestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            getTestUrl(), HttpMethod.GET, entity, String.class);

        Assert.assertEquals(expectedStatusCode, response.getStatusCode());
    }

    @Test
    public void anyJwtFreeEndpoint_shouldPass_WithoutS2SJwtToken() {

        HttpStatus expectedStatusCode = HttpStatus.OK;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        TestRestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            getTestUrl(""), HttpMethod.GET, entity, String.class);

        Assert.assertEquals(expectedStatusCode, response.getStatusCode());
    }

    private String getTestUrl(String... args) {
        if (args.length == 0) {
            return "http://localhost:" + port + "/room"; // because it's simple endpoint to use
        } else {
            return "http://localhost:" + port + String.join("", args);
        }
    }

    static class Helper {

        static final String HEADER_NAME = "Authorization";
        static final String HEADER_CONTENT_PREFIX = "Bearer ";

        HttpHeaders createEventsAuthenticationHeader() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HEADER_NAME, HEADER_CONTENT_PREFIX + createEventsToken());
            return headers;
        }

        String createEventsToken() {
            return createToken(5000, "FakeSecret1", "snl-api");
        }

        String createToken(long expiration, String secret, String serviceName) {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                .claim("service", serviceName)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        }
    }
}
