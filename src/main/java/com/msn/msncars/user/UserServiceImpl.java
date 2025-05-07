package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import com.msn.msncars.auth.keycloak.KeycloakService;
import com.msn.msncars.company.CompanyService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final CompanyService companyService;

    public UserServiceImpl(KeycloakService keycloakService, UserMapper userMapper, CompanyService companyService) {
        this.keycloakService = keycloakService;
        this.userMapper = userMapper;
        this.companyService = companyService;
    }

    @Override
    public UserBasicInformationDTO getUserBasicInformation(String userId) {
        UserRepresentation userRepresentation = keycloakService.getUserRepresentationById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        return userMapper.toUserBasicInformationDTO(userRepresentation, getAccountRoles(userId));
    }

    @Override
    public List<AccountRole> getAccountRoles(String userId) {
         List<RoleRepresentation> userRoles = keycloakService.getRoles(userId);

        return userRoles.stream()
                .map(userRole -> AccountRole.valueOf(userRole.getName().toUpperCase()))
                .toList();
    }

    public void deleteUser(String userId) {
        try(Response response = keycloakService.deleteUser(userId)) {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                logger.warn("Failed to delete user {}. Status: {}, {}", userId, response.getStatus(), response.getStatusInfo().getReasonPhrase());
                return;
            }
            companyService.cleanupCompaniesOfRemovedUser(userId);
        }
    }

}
