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
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final CompanyService companyService;

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(KeycloakService keycloakService, UserMapper userMapper, CompanyService companyService) {
        this.keycloakService = keycloakService;
        this.userMapper = userMapper;
        this.companyService = companyService;
    }

    @Override
    public UserBasicInformationDTO getUserBasicInformation(String userId) {
        logger.debug("Entering getUserBasicInformation with userId: {}", userId);

        UserRepresentation userRepresentation = keycloakService.getUserRepresentationById(userId)
                .orElseThrow(() -> {
                    logger.error("User with id: {} not found in Keycloak.", userId);
                    return new NotFoundException("User does not exist.");
                });

        logger.debug("UserRepresentation successfully retrieved from Keycloak for userId: {}", userId);

        UserBasicInformationDTO userBasicInformationDTO = userMapper.toUserBasicInformationDTO(userRepresentation, getAccountRoles(userId));

        logger.debug("UserRepresentation successfully mapped to UserBasicInformationDTO for userId: {}", userId);

        return userBasicInformationDTO;
    }

    @Override
    public List<AccountRole> getAccountRoles(String userId) {
        logger.debug("Entering getAccountRoles with userId: {}", userId);

        List<RoleRepresentation> userRoles = keycloakService.getRoles(userId);

        logger.debug("UserRoles successfully retrieved from Keycloak for userId: {}", userId);

        List<AccountRole> accountRoles = userRoles.stream()
                .map(userRole -> AccountRole.valueOf(userRole.getName().toUpperCase()))
                .toList();

        logger.debug("UserRoles successfully mapped to AccountRoles for userId: {}", userId);

        return accountRoles;
    }

    @Override
    public void deleteUser(String userId) {
        logger.debug("Entering deleteUser with userId: {}", userId);

        try(Response response = keycloakService.deleteUser(userId)) {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                logger.warn("Failed to delete user {}. Status: {}, {}", userId, response.getStatus(), response.getStatusInfo().getReasonPhrase());
                return;
            }

            logger.debug("User deleted successfully in Keycloak, starting cleanupCompaniesOfRemovedUser for userId: {}", userId);

            companyService.cleanupCompaniesOfRemovedUser(userId);

            logger.debug("Completed cleanupCompaniesOfRemovedUser for userId: {}", userId);
        }
    }

    @Override
    public void blockUser(String userEmail) {
        keycloakService.blockUser(userEmail);
    }
}
