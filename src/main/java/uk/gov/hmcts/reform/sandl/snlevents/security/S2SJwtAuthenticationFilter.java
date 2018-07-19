package uk.gov.hmcts.reform.sandl.snlevents.security;

import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class S2SJwtAuthenticationFilter extends OncePerRequestFilter {

    private final S2SAuthenticationService s2sAuth;
    private final List<String> jwtFreeEndpoints;

    @Autowired
    public S2SJwtAuthenticationFilter(S2SAuthenticationService s2sauth) {
        this.s2sAuth = s2sauth;
        this.jwtFreeEndpoints = Arrays.asList("/health", "/error", "/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (shouldDoTokenCheck(request.getRequestURI())) {
            s2sTokenCheck(request, response);
        }
        filterChain.doFilter(request, response);
    }

    private void s2sTokenCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String jwt = getJwtFromRequest(request);
            if (!(StringUtils.hasText(jwt) && s2sAuth.validateToken(jwt))) {
                throw new AuthenticationException("No Token provided");
            }
        } catch (AuthenticationException | SignatureException e) {
            logger.error("Responding with unauthorized error. Message - " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception ex) {
            logger.error("JWT token is invalid", ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }


    private boolean shouldDoTokenCheck(String urlPath) {
        for (String element : this.jwtFreeEndpoints) {
            if (urlPath.startsWith(element)) {
                return false;
            }
        }
        return true;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(S2SAuthenticationService.HEADER_NAME);
        if (StringUtils.hasText(bearerToken)
            && bearerToken.startsWith(S2SAuthenticationService.HEADER_CONTENT_PREFIX)
            ) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
