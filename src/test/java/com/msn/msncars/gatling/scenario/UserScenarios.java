package com.msn.msncars.gatling.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.http.HttpDsl;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.http.HttpDsl.http;

public class UserScenarios {

    private static final FeederBuilder<String> loginFeeder = CoreDsl.csv("gatling/test_users.csv").circular();

    public static final ChainBuilder authenticate =
            feed(loginFeeder)
            .exec(authenticateByUsernameAndPassword("#{username}", "#{password}"));

    public static ChainBuilder authenticateByUsernameAndPassword(String username, String password) {
        return exec(
                http("Get User JwtToken")
                        .post("http://localhost:8081/realms/MSNcars/protocol/openid-connect/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("client_id", "MSNcars")
                        .formParam("username", username)
                        .formParam("password", password)
                        .check(
                                HttpDsl.status().is(200),
                                CoreDsl.jsonPath("$.access_token").saveAs("jwtToken")
                        )
        );
    }

    public static ChainBuilder deleteUser(String username, String password) {
        return exec(authenticateByUsernameAndPassword(username, password))
                .exec(
                        http("Delete User")
                                .delete("http://localhost:8080/user")
                                .header("Authorization", "Bearer #{jwtToken}")
                                .check(HttpDsl.status().is(200))
        );
    }

    public static ChainBuilder deleteUser(String jwtToken) {
        return exec(
                http("Delete User")
                        .delete("http://localhost:8080/user")
                        .header("Authorization", "Bearer " + jwtToken)
                        .check(HttpDsl.status().is(200))
        );
    }
}
