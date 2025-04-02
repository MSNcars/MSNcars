package com.msn.msncars.auth;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService{

    private final Keycloak keycloakAPI;
    
    public AuthServiceImpl(Keycloak keycloakAPI) {
        this.keycloakAPI = keycloakAPI;
    }
    
    public ResponseEntity<String> registerUser(RegisterRequest registerRequest) {
        UserRepresentation user = getUserRepresentation(registerRequest);

        //Send request to keycloak API
        try(Response response = keycloakAPI.realm("MSNcars").users().create(user)){
            if (response.getStatus() == 201) {
                return ResponseEntity.ok("User registered successfully");
            } else {
                return ResponseEntity.status(response.getStatus()).body("Error: " + response.getStatusInfo().getReasonPhrase());
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
