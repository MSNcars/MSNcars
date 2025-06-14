package com.msn.msncars.gatling.prerequisites;

import jakarta.ws.rs.core.Response;
import org.jetbrains.annotations.NotNull;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Configuration
public class PerformanceTestUserManager {

    private static final String TEST_USER_PREFIX = "performance_test_user__";
    private static final String TEST_USER_PASSWORD = "testPassword123";
    private static final String TEST_USERS_CSV_FILE_PATH = "src/test/resources/gatling/test_users.csv";

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        Properties properties = readAppProperties();

        String serverUrl = properties.getProperty("keycloak.server.url");
        String realmName = properties.getProperty("keycloak.realm");
        String clientId = "admin-cli";
        String adminUsername = properties.getProperty("keycloak.credentials.username");
        String adminPassword = properties.getProperty("keycloak.credentials.password");

        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build()) {

            RealmResource realm = keycloak.realm(realmName);

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        System.out.println("Please specify number of users to create.");
                        break;
                    }
                    int count = Integer.parseInt(args[1]);
                    createUsers(realm, count);
                    break;
                case "delete":
                    deleteUsers(realm);
                    break;
                default:
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createUsers(RealmResource realm, int count) {
        createUsersCsvFile();
        for (int i = 0; i < count; i++) {
            var user = getUserRepresentation(i);

            Response response = realm.users().create(user);
            System.out.println("Created user " + TEST_USER_PREFIX + i + ": HTTP " + response.getStatus());
            if (response.getStatus() == Response.Status.CREATED.getStatusCode())
                addUserToCsvFile(TEST_USER_PREFIX + i, CreatedResponseUtil.getCreatedId(response));
            response.close();
        }
    }

    @NotNull
    private static UserRepresentation getUserRepresentation(int i) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(TEST_USER_PREFIX + i);
        user.setFirstName(TEST_USER_PREFIX + i);
        user.setLastName(TEST_USER_PREFIX + i);
        user.setEmail(TEST_USER_PREFIX + i + "@test.com");
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(TEST_USER_PASSWORD);
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    private static void deleteUsers(RealmResource realm) {
        List<UserRepresentation> users;
        do {
            users = realm.users().searchByUsername(TEST_USER_PREFIX, false);
            for (UserRepresentation user : users) {
                realm.users().get(user.getId()).remove();
                System.out.println("Deleted user: " + user.getUsername());
            }
        } while (!users.isEmpty());

        deleteUsersCsvFile();
    }

    private static void createUsersCsvFile() {
        try {
            Files.writeString(Paths.get(TEST_USERS_CSV_FILE_PATH), "username,password,user_id\n");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void addUserToCsvFile(String username, String userId) {
        Path path = Paths.get(TEST_USERS_CSV_FILE_PATH);
        try {
            Files.writeString(path, String.format("%s,%s,%s%n", username, TEST_USER_PASSWORD, userId), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void deleteUsersCsvFile() {
        Path path = Paths.get(TEST_USERS_CSV_FILE_PATH);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("In order to use this class to create test users, you must specify parameters of run configuration:");
        System.out.println("create <number> - create test users");
        System.out.println("delete - delete test users");
    }

    private static Properties readAppProperties() {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("src/main/resources/application.properties"))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
        return properties;
    }
}
