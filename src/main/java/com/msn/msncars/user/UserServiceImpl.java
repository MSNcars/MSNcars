package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import com.msn.msncars.auth.keycloak.KeycloakConfig;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyDTO;
import com.msn.msncars.company.CompanyMapper;
import com.msn.msncars.company.CompanyService;
import com.msn.msncars.usercompany.UserCompanyService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserCompanyService userCompanyService;

    public UserServiceImpl(Keycloak keycloakAPI, KeycloakConfig keycloakConfig, UserMapper userMapper, UserCompanyService userCompanyService) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
        this.userMapper = userMapper;
        this.userCompanyService = userCompanyService;
    }

    @Override
    public UserDTO getBasicUserInformation(String userId) {
        UserRepresentation userRepresentation = getUserRepresentationById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        return userMapper.toDTO(userRepresentation);
    }

    @Override
    public List<AccountRole> getUserRoles(String userId) {
         ClientRepresentation client = keycloakAPI.realm(keycloakConfig.getRealm())
                 .clients()
                 .findByClientId(keycloakConfig.getRealm())
                 .getFirst();

        List<RoleRepresentation> userRoles = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .roles()
                .clientLevel(client.getId())
                .listAll();

        return userRoles.stream()
                .map(userRole -> AccountRole.valueOf(userRole.getName().toUpperCase()))
                .toList();
    }

    @Override
    public Optional<UserRepresentation> getUserRepresentationById(String userId) {
        try {
            return Optional.of(keycloakAPI.realm(keycloakConfig.getRealm())
                    .users()
                    .get(userId)
                    .toRepresentation());
        } catch (NotFoundException nfe) {
            return Optional.empty();
        }
    }

    @Override
    public List<CompanyDTO> getCompaniesUserBelongsTo(String userId) {
        if (getUserRepresentationById(userId).isEmpty())
            throw new NotFoundException("User does not exist.");
        return userCompanyService.getCompaniesUserBelongsTo(userId);
    }

    public void deleteUser(String userId) {
        try(Response response = keycloakAPI.realm(keycloakConfig.getRealm()).users().delete(userId)) {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                logger.warn("Failed to delete user {}. Status: {}, {}", userId, response.getStatus(), response.getStatusInfo().getReasonPhrase());
                return;
            }
            userCompanyService.cleanupCompaniesUserBelongsTo(userId);
        }
    }

}
