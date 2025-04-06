package com.msn.msncars.user;

import com.msn.msncars.auth.keycloak.KeycloakConfig;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;

    public UserService(Keycloak keycloakAPI, KeycloakConfig keycloakConfig) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
    }

    public UserRepresentation getUserRepresentationById(String userId) {
        return keycloakAPI.realm(keycloakConfig.getRealm()).users().get(userId).toRepresentation();
    }
}
