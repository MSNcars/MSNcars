package com.msn.msncars.auth;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class AuthConfig {

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    @Value("${keycloak.credentials.username}")
    private String keycloakUsername;

    @Value("${keycloak.credentials.password}")
    private String keycloakPassword;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.GET, "/images", "/listings/{id}/images", "/public", "/listings")
                                .permitAll()
                                .requestMatchers("/auth/register", "/swagger-ui/**", "/v3/api-docs/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    Keycloak keycloakAPI() {
        return Keycloak.getInstance(
                keycloakServerUrl,
                "master",
                keycloakUsername,
                keycloakPassword,
                "admin-cli"
        );
    }
}
