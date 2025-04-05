package com.msn.msncars.user;

import org.keycloak.representations.idm.UserRepresentation;

public class UserMapper {
    private UserMapper() {}
    public static UserDTO toDTO(UserRepresentation userRepresentation) {
        return new UserDTO(
                userRepresentation.getId(),
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getUsername()
        );
    }
}
