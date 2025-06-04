package com.msn.msncars.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AuthConfig {
  
    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    public AuthConfig(KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.GET, "/images", "/listings/{id}/images", "/public")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/listings/**")
                                .permitAll()
                                .requestMatchers("/auth/user/register", "/auth/company/register", "/swagger-ui/**", "/v3/api-docs/**")
                                .permitAll()
                                .requestMatchers("/company/**")
                                .permitAll()
                                .requestMatchers("/make/**")
                                .permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/user")
                                .hasAnyRole("user", "company")
                                .requestMatchers("/invitations")
                                .hasRole("company")
                                .requestMatchers("/admin")
                                .hasRole("admin")
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
                );
        return http.build();
    }
}
