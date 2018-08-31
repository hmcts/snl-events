package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
public class TokenService {

    static final String HEADER_NAME = "Authorization";
    static final String HEADER_CONTENT_PREFIX = "Bearer ";

    private String secret;
    private long expiration;

    public TokenService(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String createToken(String serviceName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .claim("service", serviceName)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }

    public Claims extractToken(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            return null;
        }
        try {
            return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(authToken)
                .getBody();
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.", ex);
        }

        return null;
    }
}
