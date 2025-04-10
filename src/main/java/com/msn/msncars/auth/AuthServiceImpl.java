package com.msn.msncars.auth;
import com.msn.msncars.auth.keycloak.KeycloakConfig;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService{

    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;
    
    public AuthServiceImpl(Keycloak keycloakAPI, KeycloakConfig keycloakConfig) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
    }

    /*
        Creates new account and assign user role to it, this has to be done in separate api calls.
     */
    public ResponseEntity<String> registerUser(RegisterRequest registerRequest) {
        UserRepresentation user = getUserRepresentation(registerRequest);

        //Send request to register user
        try(Response registerResponse = keycloakAPI.realm(keycloakConfig.getRealm()).users().create(user)){
            if (registerResponse.getStatus() == 201) {

                //Get client and role
                ClientRepresentation clientRepresentation = keycloakAPI.realm(keycloakConfig.getRealm())
                        .clients().findByClientId(keycloakConfig.getRealm())
                        .getFirst();
                RoleRepresentation roleRepresentation = keycloakAPI.realm(keycloakConfig.getRealm())
                        .clients().get(clientRepresentation.getId())
                        .roles().get("user")
                        .toRepresentation();

                //Add user role
                keycloakAPI.realm(keycloakConfig.getRealm())
                        .users().get(CreatedResponseUtil.getCreatedId(registerResponse))
                        .roles().clientLevel(clientRepresentation.getId()).add(List.of(roleRepresentation));

                return ResponseEntity.ok("User registered successfully");
            } else {
                return ResponseEntity.status(registerResponse.getStatus()).body("Error: " + registerResponse.getStatusInfo().getReasonPhrase());
            }
        }
    }
    private UserRepresentation getUserRepresentation(RegisterRequest registerRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerRequest.username());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        user.setEmail(registerRequest.email());
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(registerRequest.password());
        user.setCredentials(Collections.singletonList(passwordCred));

        return user;
    }
}
