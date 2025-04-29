package com.msn.msncars.user;

import com.msn.msncars.auth.keycloak.KeycloakConfig;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(Keycloak keycloakAPI, KeycloakConfig keycloakConfig) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
    }

    public UserRepresentation getUserRepresentationById(String userId) {
        return keycloakAPI.realm(keycloakConfig.getRealm()).users().get(userId).toRepresentation();
    }

    public void deleteUser(String userId) {
        try(Response response = keycloakAPI.realm(keycloakConfig.getRealm()).users().delete(userId)) {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode())
                logger.warn("Failed to delete user {}. Status: {}, {}", userId, response.getStatus(), response.getStatusInfo().getReasonPhrase());
        }
    }
}
