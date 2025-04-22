package com.msn.msncars.user;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserRepresentation fromDTO(UserDTO userDTO);
    UserDTO toDTO(UserRepresentation userRepresentation);
}
