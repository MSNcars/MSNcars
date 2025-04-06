package com.msn.msncars.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server.url}")
    private String serverUrl;

    @Value("${keycloak.credentials.username}")
    private String username;

    @Value("${keycloak.credentials.password}")
    private String password;

    public String getRealm() {
        return realm;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
