package com.msn.msncars.auth.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakApiClient {

    private final KeycloakConfig keycloakConfig;

    public KeycloakApiClient(KeycloakConfig keycloakConfig) {
        this.keycloakConfig = keycloakConfig;
    }

    @Bean
    public Keycloak keycloakAPI() {
        return Keycloak.getInstance(
                keycloakConfig.getServerUrl(),
                "master",
                keycloakConfig.getUsername(),
                keycloakConfig.getPassword(),
                "admin-cli"
        );
    }

}
