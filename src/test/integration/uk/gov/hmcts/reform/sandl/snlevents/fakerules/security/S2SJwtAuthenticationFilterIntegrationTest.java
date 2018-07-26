package uk.gov.hmcts.reform.sandl.snlevents.fakerules.security;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationService;

public class S2SJwtAuthenticationFilterIntegrationTest extends BaseIntegrationTestWithFakeRules {

    @LocalServerPort
    private int port;

    @Autowired
    S2SAuthenticationService s2sAuthService;

    @Test
    public void getRoom_orAnyOther_shouldPass_WithValidS2SJwtToken() {

        HttpStatus expectedStatusCode = HttpStatus.OK;

        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        TestRestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            getTestUrl(), HttpMethod.GET, entity, String.class);

        Assert.assertEquals(expectedStatusCode, response.getStatusCode());
    }

    @Test
    public void getRoom_orAnyOther_shouldFail_WithInvalidS2SJwtToken() {

        HttpStatus expectedStatusCode = HttpStatus.OK;

        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        headers.set("Authorization", "INVALID");
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
            getTestUrl(), HttpMethod.GET, entity, String.class);

        Assert.assertEquals(expectedStatusCode, response.getStatusCode());
    }

    private String getTestUrl() {
        return "http://localhost:" + port + "/room"; // because it's simple endpoint to use
    }
}
