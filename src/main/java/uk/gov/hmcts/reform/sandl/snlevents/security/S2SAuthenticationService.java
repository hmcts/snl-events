package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class S2SAuthenticationService {

    static final String HEADER_NAME = "Authorization";
    static final String HEADER_CONTENT_PREFIX = "Bearer ";
    private static final Set<String> approvedServicesNames = Collections.singleton("snl-api");
    private final S2SAuthenticationConfig config;
    private final TokenCreator tokenCreator;

    @Autowired
    S2SAuthenticationService(S2SAuthenticationConfig config) {
        this.config = config;
        this.tokenCreator = new TokenCreator(
            config.getRules().getJwtSecret(), config.getRules().getJwtExpirationInMs(), "snl-events");
    }

    public HttpHeaders createRulesAuthenticationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, HEADER_CONTENT_PREFIX + this.tokenCreator.createToken());
        return headers;
    }

    public boolean validateToken(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            return false;
        }
        try {
            final Claims claims = Jwts.parser()
                .setSigningKey(config.getEvents().getJwtSecret())
                .parseClaimsJws(authToken)
                .getBody();
            final String serviceName = (String) claims.get("service");
            final String userName = (String) claims.get("user");
            boolean validService = approvedServicesNames.contains(serviceName);
            long millisDifference = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            if (config.getEvents().getJwtExpirationInMs() != millisDifference) {
                throw new TokenExpirationInvalidException();
            }

            if (!validService) {
                throw new TokenClientServiceInvalidException(serviceName);
            }

            String currentPrincipalName = String.format("%s:%s", serviceName, userName);
            Authentication authentication = new UsernamePasswordAuthenticationToken(currentPrincipalName,
                null, Arrays.asList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.", ex);
        } catch (TokenExpirationInvalidException ex) {
            log.error("JWT token expiration not equal to service jwt expiration.", ex);
        } catch (TokenClientServiceInvalidException ex) {
            log.error("JWT token service unknown.", ex);
        }
        return false;
    }

    class TokenCreator {
        private String secret;
        private long expiration;
        private String serviceName;

        TokenCreator(String secret, long expiration, String serviceName) {
            this.secret = secret;
            this.expiration = expiration;
            this.serviceName = serviceName;
        }

        String createToken() {
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
