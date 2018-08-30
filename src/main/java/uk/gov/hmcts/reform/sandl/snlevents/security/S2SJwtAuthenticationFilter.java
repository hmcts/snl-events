package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class S2SJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> approvedServicesNames = Collections.singleton("snl-api");

    private final S2SAuthenticationConfig s2SAuthenticationConfig;
    private final TokenService tokenService;

    @Autowired
    public S2SJwtAuthenticationFilter(S2SAuthenticationConfig s2SAuthenticationConfig) {
        this.s2SAuthenticationConfig = s2SAuthenticationConfig;
        this.tokenService = new TokenService(
            s2SAuthenticationConfig.getEvents().getJwtSecret(),
            s2SAuthenticationConfig.getEvents().getJwtExpirationInMs());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        s2sAuthenticate(request, response);
        filterChain.doFilter(request, response);
    }

    private void s2sAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        String jwt = getJwtFromRequest(request);
        Claims claims = tokenService.extractToken(jwt);
        if (claims != null && isValid(claims)) {
            authenticate(claims);
        }
    }

    private boolean isValid(Claims claims) {
        final String serviceName = (String) claims.get("service");

        boolean validService = approvedServicesNames.contains(serviceName);
        Date now = new Date();
        long millisDifference = now.getTime() - claims.getIssuedAt().getTime();

        boolean notExpired = millisDifference <= s2SAuthenticationConfig.getEvents().getJwtExpirationInMs();

        return validService && notExpired;
    }

    private void authenticate(Claims claims) {
        final String serviceName = (String) claims.get("service");
        final String userName = (String) claims.get("user");

        String currentPrincipalName = String.format("%s:%s", serviceName, userName);
        Authentication authentication = new UsernamePasswordAuthenticationToken(currentPrincipalName,
            null, Arrays.asList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TokenService.HEADER_NAME);
        if (StringUtils.hasText(bearerToken)
            && bearerToken.startsWith(TokenService.HEADER_CONTENT_PREFIX)) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
