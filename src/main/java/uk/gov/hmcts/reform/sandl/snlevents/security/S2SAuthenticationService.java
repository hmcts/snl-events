package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class S2SAuthenticationService {

    static final String HEADER_NAME = "Authorization";
    static final String HEADER_CONTENT_PREFIX = "Bearer ";
    private static final Set<String> approvedServicesNames = Collections.singleton("snl-api");
    private static final String thisServiceName = "snl-events";
    private final String localJwtSecret;
    private final int localJwtExpirationInMs;
    private final String rulesJwtSecret;
    private final int rulesJwtExpirationInMs;

    public S2SAuthenticationService(
        @Value("${management.security.events.jwtSecret}") final String localJwtSecret,
        @Value("${management.security.events.jwtExpirationInMs}") final int localJwtExpirationInMs,
        @Value("${management.security.rules.jwtSecret}") final String rulesJwtSecret,
        @Value("${management.security.rules.jwtExpirationInMs}") final int rulesJwtExpirationInMs
    ) {
        this.localJwtSecret = localJwtSecret;
        this.localJwtExpirationInMs = localJwtExpirationInMs;
        this.rulesJwtSecret = rulesJwtSecret;
        this.rulesJwtExpirationInMs = rulesJwtExpirationInMs;
    }

    public HttpHeaders createRulesAuthenticationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, HEADER_CONTENT_PREFIX + this.createRulesToken());
        return headers;
    }

    public boolean validateToken(String authToken) {
        try {
            final Claims claims = Jwts.parser()
                .setSigningKey(localJwtSecret)
                .parseClaimsJws(authToken)
                .getBody();
            final String serviceName = (String) claims
                .get("service");
            boolean valid = approvedServicesNames.contains(serviceName);
            long millisDifference = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            return valid && localJwtExpirationInMs == millisDifference;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.", ex);
        }
        return false;
    }

    private String createRulesToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + rulesJwtExpirationInMs);

        return Jwts.builder()
            .claim("service", thisServiceName)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, rulesJwtSecret)
            .compact();
    }
}
