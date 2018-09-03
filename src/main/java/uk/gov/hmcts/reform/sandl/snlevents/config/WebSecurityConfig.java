package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SAuthenticationConfig;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SJwtAuthenticationFilter;
import uk.gov.hmcts.reform.sandl.snlevents.security.ServiceAuthenticationEntryPoint;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ServiceAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private S2SAuthenticationConfig s2SAuthenticationConfig;

    @Bean
    public S2SJwtAuthenticationFilter jwtAuthenticationFilter() {
        return new S2SJwtAuthenticationFilter(s2SAuthenticationConfig);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/health", "/error", "/info", "/")
                .permitAll()
                .and()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .csrf()
                .disable()
            .cors();

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}
