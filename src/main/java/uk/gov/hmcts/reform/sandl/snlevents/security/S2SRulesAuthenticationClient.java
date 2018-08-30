package uk.gov.hmcts.reform.sandl.snlevents.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class S2SRulesAuthenticationClient {
    private final TokenService tokenService;

    @Autowired
    S2SRulesAuthenticationClient(S2SAuthenticationConfig config) {
        this.tokenService = new TokenService(
            config.getRules().getJwtSecret(),
            config.getRules().getJwtExpirationInMs());
    }

    public HttpHeaders createRulesAuthenticationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TokenService.HEADER_NAME,
            TokenService.HEADER_CONTENT_PREFIX
                + this.tokenService.createToken("snl-events"));
        return headers;
    }
}
