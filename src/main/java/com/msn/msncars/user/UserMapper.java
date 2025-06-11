package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserRepresentation fromDTO(UserDTO userDTO);
    UserDTO toUserDTO(UserRepresentation userRepresentation);
    UserBasicInformationDTO toUserBasicInformationDTO(UserRepresentation userRepresentation, List<AccountRole> accountRoles);
}
