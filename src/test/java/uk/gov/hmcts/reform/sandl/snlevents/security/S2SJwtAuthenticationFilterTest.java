package uk.gov.hmcts.reform.sandl.snlevents.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class S2SJwtAuthenticationFilterTest {

    private static final String SECRET_EVENTS = "SecretE";
    private static final int DEFAULT_EXPIRY = 5000;

    private S2SAuthenticationConfig s2SAuthenticationConfig;
    private S2SJwtAuthenticationFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private TokenService clientTokenService;

    @Before
    public void setup() {
        s2SAuthenticationConfig = Mockito.mock(S2SAuthenticationConfig.class);
        when(s2SAuthenticationConfig.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, DEFAULT_EXPIRY));

        filter = new S2SJwtAuthenticationFilter(s2SAuthenticationConfig);
        clientTokenService = new TokenService(SECRET_EVENTS, DEFAULT_EXPIRY);
        MockitoAnnotations.initMocks(this);

        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_notNeededAuth_forOpenEndpoint() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn("NO BEARER IN HEADER");
        when(request.getRequestURI())
            .thenReturn("/health"); //

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forNoToken() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forInvalidTokenInProperHeader() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn(TokenService.HEADER_CONTENT_PREFIX + "INVALIDTOKEN");
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forNoBearerTextInHeader() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn("NO BEARER IN HEADER");
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forNoValueInHeader() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn("");
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forExpiredToken() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn(TokenService.HEADER_CONTENT_PREFIX
                + clientTokenService.createToken("snl-api"));
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        when(s2SAuthenticationConfig.getEvents())
            .thenReturn(new S2SAuthenticationConfig.JwtCredentials(SECRET_EVENTS, -10));

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_notAuthenticated_forUnknownService() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn(TokenService.HEADER_CONTENT_PREFIX
                + clientTokenService.createToken("UNKNOWN"));
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        filter.doFilterInternal(request, response, filterChain);

        assertThatNotAuthenticated();
    }

    @Test
    public void doFilterInternal_Authenticated_forValidToken() throws ServletException, IOException {
        when(request.getHeader(TokenService.HEADER_NAME))
            .thenReturn(TokenService.HEADER_CONTENT_PREFIX
                + clientTokenService.createToken("snl-api"));
        when(request.getRequestURI())
            .thenReturn("/needingJwtUrl");

        filter.doFilterInternal(request, response, filterChain);

        assertThatAuthenticated();
    }

    private void assertThatNotAuthenticated() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private void assertThatAuthenticated() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
