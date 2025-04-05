package com.msn.msncars.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakBean {

    private final KeycloakConfig keycloakConfig;

    public KeycloakBean(KeycloakConfig keycloakConfig) {
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
