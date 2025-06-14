package com.msn.msncars.auth.keycloak;

import com.msn.msncars.user.UserNotFoundException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class KeycloakService {
    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;

    public KeycloakService(Keycloak keycloak, KeycloakConfig keycloakConfig) {
        this.keycloak = keycloak;
        this.keycloakConfig = keycloakConfig;
    }

    public Optional<UserRepresentation> getUserRepresentationById(String userId) {
        try {
            return Optional.of(keycloak.realm(keycloakConfig.getRealm())
                    .users()
                    .get(userId)
                    .toRepresentation());
        } catch (NotFoundException nfe) {
            return Optional.empty();
        }
    }

    public List<RoleRepresentation> getRoles(String userId) {
        ClientRepresentation clientRepresentation = keycloak.realm(keycloakConfig.getRealm())
                .clients()
                .findByClientId(keycloakConfig.getRealm())
                .getFirst();

        return keycloak.realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .roles()
                .clientLevel(clientRepresentation.getId())
                .listAll();
    }

    public Response deleteUser(String userId) {
        return keycloak.realm(keycloakConfig.getRealm())
                .users()
                .delete(userId);
    }

    public void blockUser(String userEmail){
        UserRepresentation userRepresentation;
        try{
            userRepresentation = keycloak.realm(keycloakConfig.getRealm())
                    .users()
                    .searchByEmail(userEmail, true)
                    .getFirst();
        }catch (NoSuchElementException e){
            throw new UserNotFoundException(String.format("User with userEmail %s was not found", userEmail));
        }

        userRepresentation.setEnabled(false);

        keycloak.realm(keycloakConfig.getRealm())
                .users()
                .get(userRepresentation.getId())
                .update(userRepresentation);
    }

}
