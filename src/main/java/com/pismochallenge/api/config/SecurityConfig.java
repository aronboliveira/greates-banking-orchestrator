package com.pismochallenge.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security — JWT bearer token validation.
 *
 * <p>Active only in the {@code eks} profile. Tests and local development run
 * with the default (no-security) Spring Boot auto-configuration; the existing
 * controller test suite is unaffected.
 *
 * <p>Configuration of the JWKS URI is done via
 * {@code application-eks.properties} → {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}
 * which is populated by the ConfigMap. Spring Boot auto-configures the
 * {@code JwtDecoder} bean from that property.
 */
@Configuration
@EnableWebSecurity
@Profile("eks")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health/**",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .build();
    }
}
