package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserRepresentation fromDTO(UserDTO userDTO);
    UserDTO toUserDTO(UserRepresentation userRepresentation);
//    @Mapping(target = "accountRoles", source = "accountRoles")
    UserBasicInformationDTO toUserBasicInformationDTO(UserRepresentation userRepresentation, List<AccountRole> accountRoles);
}
