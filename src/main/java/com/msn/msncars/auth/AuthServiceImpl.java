package com.msn.msncars.auth;

import com.msn.msncars.auth.dto.CompanyRegistrationRequest;
import com.msn.msncars.auth.dto.CompanyRegistrationResponse;
import com.msn.msncars.auth.dto.UserRegistrationRequest;
import com.msn.msncars.auth.exception.RegistrationException;
import com.msn.msncars.auth.keycloak.KeycloakConfig;
import com.msn.msncars.company.CompanyDTO;
import com.msn.msncars.company.CompanyService;
import com.msn.msncars.user.UserService;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AuthServiceImpl implements AuthService{

    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;
    private final CompanyService companyService;
    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(Keycloak keycloakAPI, KeycloakConfig keycloakConfig, CompanyService companyService, UserService userService) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
        this.companyService = companyService;
        this.userService = userService;
    }

    @Override
    public String registerUserAndAssignRole(UserRegistrationRequest userRegistrationRequest, AccountRole accountRole) {
        logger.debug("Entering registerUserAndAssignRole method with role: {}", accountRole);

        UserRepresentation userRepresentation = getUserRepresentation(userRegistrationRequest);

        logger.debug("UserRepresentation created out of userRegistrationRequest.");

        String userId = createUser(userRepresentation);

        logger.debug("User created out of userRepresentation, userId: {}", userId);

        assignRoleToUser(userId, accountRole);

        logger.debug("Role {} assigned to user.", accountRole);

        return userId;
    }

    @Override
    public CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest companyRegistrationRequest) {
        logger.debug("Entering registerCompany method.");

        String userId = registerUserAndAssignRole(companyRegistrationRequest.userRegistrationRequest(), AccountRole.COMPANY);

        logger.debug("User account created, userId: {}", userId);

        Long companyId;
        try {
            CompanyDTO company = companyService.createCompany(companyRegistrationRequest.companyCreationRequest(), userId);
            companyId = company.id();
        } catch (Exception e) {
            logger.debug("Error while creating company", e);
            userService.deleteUser(userId);
            logger.debug("User account deleted, userId: {}", userId);
            throw e;
        }

        logger.debug("Company created, companyId: {}", companyId);

        return new CompanyRegistrationResponse(userId, companyId);
    }

    private String createUser(UserRepresentation userRepresentation) {
        Response registerResponse = attemptUserCreation(userRepresentation);

        // If in previous requests user was created successfully, but something went wrong when assigning role to user
        // then you can safely remove this user -> it is needed because creating user and adding role to user
        // using keycloakApi is not an atomic operation
        if (registerResponse.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            registerResponse.close();
            cleanupUsersIfNecessary(userRepresentation);

            try (Response retryResponse = attemptUserCreation(userRepresentation)) {
                return handleUserCreationResponse(retryResponse);
            }
        }

        try (registerResponse) {
            return handleUserCreationResponse(registerResponse);
        }
    }

    private Response attemptUserCreation(UserRepresentation userRepresentation) {
        return keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .create(userRepresentation);
    }

    private String handleUserCreationResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode())
            throw new RegistrationException(
                response.getStatusInfo().getReasonPhrase(),
                HttpStatus.valueOf(response.getStatus())
            );

        return CreatedResponseUtil.getCreatedId(response);
    }

    private void assignRoleToUser(String userId, AccountRole accountRole) {
        ClientRepresentation client = keycloakAPI.realm(keycloakConfig.getRealm())
                .clients()
                .findByClientId(keycloakConfig.getRealm())
                .getFirst();

        RoleRepresentation role = keycloakAPI.realm(keycloakConfig.getRealm())
                .clients()
                .get(client.getId())
                .roles()
                .get(accountRole.getName())
                .toRepresentation();

        keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .roles()
                .clientLevel(client.getId())
                .add(List.of(role));
    }

    private UserRepresentation getUserRepresentation(UserRegistrationRequest userRegistrationRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRegistrationRequest.username());
        user.setFirstName(userRegistrationRequest.firstName());
        user.setLastName(userRegistrationRequest.lastName());
        user.setEmail(userRegistrationRequest.email());
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userRegistrationRequest.password());
        user.setCredentials(Collections.singletonList(passwordCred));

        return user;
    }

    private void cleanupUsersIfNecessary(UserRepresentation userRepresentation) {
        List<UserRepresentation> usersWithSameUsernameOrEmail = getUsersWithSameUsernameOrEmail(userRepresentation.getUsername(), userRepresentation.getEmail());
        if (!usersWithSameUsernameOrEmail.isEmpty())
            removeUsersWithoutAnyAccountRole(usersWithSameUsernameOrEmail);
    }

    private List<UserRepresentation> getUsersWithSameUsernameOrEmail(String username, String email) {
        List<UserRepresentation> usersWithSameUsername = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .searchByUsername(username, true);

        List<UserRepresentation> usersWithSameEmail = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .searchByEmail(email, true);

        // max 1 person can have same username and max 1 person can have same email, but they may be the same person

        if (!usersWithSameUsername.isEmpty() && !usersWithSameEmail.isEmpty()) {
            UserRepresentation user1 = usersWithSameUsername.getFirst();
            UserRepresentation user2 = usersWithSameEmail.getFirst();

            if (user1.getId().equals(user2.getId()))
                return List.of(user1);
        }

        return Stream.concat(usersWithSameUsername.stream(), usersWithSameEmail.stream()).toList();
    }

    private void removeUsersWithoutAnyAccountRole(List<UserRepresentation> users) {
        for (var user: users)
            if (!doesUserHaveAnyAccountRole(user.getId()))
                userService.deleteUser(user.getId());
    }

    private boolean doesUserHaveAnyAccountRole(String userId) {
        List<AccountRole> accountRoles = userService.getAccountRoles(userId);
        return !accountRoles.isEmpty();
    }
}
