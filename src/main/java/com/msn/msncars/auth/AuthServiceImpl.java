package com.msn.msncars.auth;

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

    public AuthServiceImpl(Keycloak keycloakAPI, KeycloakConfig keycloakConfig, CompanyService companyService, UserService userService) {
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
        this.companyService = companyService;
        this.userService = userService;
    }

    @Override
    public String registerUserAndAssignRole(UserRegistrationRequest userRegistrationRequest, AccountRole accountRole) {
        UserRepresentation userRepresentation = getUserRepresentation(userRegistrationRequest);
        cleanupUsersIfNecessary(userRepresentation);
        String userId = createUser(userRepresentation);
        assignRoleToUser(userId, accountRole);
        return userId;
    }

    @Override
    public CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest companyRegistrationRequest) {
        String userId = registerUserAndAssignRole(companyRegistrationRequest.userRegistrationRequest(), AccountRole.COMPANY);
        Long companyId;
        try {
            CompanyDTO company = companyService.createCompany(companyRegistrationRequest.companyCreationRequest(), userId);
            companyId = company.id();
        } catch (Exception e) {
            userService.deleteUser(userId);
            throw e;
        }
        return new CompanyRegistrationResponse(userId, companyId);
    }

    private String createUser(UserRepresentation userRepresentation) {
        try (Response registerResponse = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .create(userRepresentation)) {

            if (registerResponse.getStatus() != 201)
                throw new RegistrationException(
                        registerResponse.getStatusInfo().getReasonPhrase(),
                        HttpStatus.valueOf(registerResponse.getStatus())
                );

            return CreatedResponseUtil.getCreatedId(registerResponse);
        }
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

        return Stream.concat(usersWithSameUsername.stream(), usersWithSameEmail.stream()).toList();
    }

    private void removeUsersWithoutAnyAccountRole(List<UserRepresentation> users) {
        for (var user: users)
            if (!doesUserHaveAnyAccountRole(user.getId()))
                userService.deleteUser(user.getId());
    }

    private boolean doesUserHaveAccountRole(String userId, AccountRole accountRole) {
        ClientRepresentation client = keycloakAPI.realm(keycloakConfig.getRealm())
                .clients()
                .findByClientId(keycloakConfig.getRealm())
                .getFirst();

        List<RoleRepresentation> roles = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .roles()
                .clientLevel(client.getId())
                .listAll();

        return roles.stream()
                .anyMatch(role -> role.getName().equals(accountRole.getName()));
    }

    private boolean doesUserHaveAnyAccountRole(String userId) {
        for (var accountRole : AccountRole.values())
            if (doesUserHaveAccountRole(userId, accountRole))
                return true;
        return false;
    }
}
