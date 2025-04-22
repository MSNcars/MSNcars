package com.msn.msncars.user;

import com.msn.msncars.auth.keycloak.KeycloakConfig;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;

    public UserService(Keycloak keycloakAPI, KeycloakConfig keycloakConfig) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
    }

    public Optional<UserRepresentation> getUserRepresentationById(String userId) {
        try {
            return Optional.of(keycloakAPI.realm(keycloakConfig.getRealm())
                    .users()
                    .get(userId)
                    .toRepresentation());
        } catch (NotFoundException nfe) {
            return Optional.empty();
        }
    }
}
