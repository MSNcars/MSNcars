package com.msn.msncars.gatling.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.msn.msncars.gatling.scenario.CompanyRegistrationScenario.registerCompany;
import static com.msn.msncars.gatling.scenario.UserScenarios.authenticateByUsernameAndPassword;
import static com.msn.msncars.gatling.scenario.UserScenarios.deleteUser;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class InvitationOperationsSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http
                    .baseUrl("http://localhost:80/invitations")
                    .header("Content-Type", "application/json");

    private static final int USERS_AMOUNT = Integer.parseInt(System.getProperty("users", "50"));
    private static final int DURATION_SECONDS = Integer.parseInt(System.getProperty("duration", "30"));

    private final FeederBuilder<String> userFeeder = CoreDsl.csv("./gatling/test_users.csv").circular();

    private final ChainBuilder invite =
            exec(
                    http("Create Invitation")
                            .post("")
                            .header("Authorization", "Bearer #{jwtTokenCompanyAccount}")
                            .body(StringBody("""
                                    {
                                    "recipientId": "#{recipientId}",
                                    "senderCompanyId": #{senderCompanyId}
                                    }
                                    """))
                            .check(CoreDsl.jsonPath("$.id").saveAs("invitationId"))
            );

    private final ChainBuilder acceptInvitation =
            exec(
                    http("Accept Invitation")
                            .post("/#{invitationId}/accept")
                            .header("Authorization", "Bearer #{jwtTokenRecipientAccount}")
            );

    private final ChainBuilder declineInvitation =
            exec(
                    http("Decline Invitation")
                            .post("/#{invitationId}/decline")
                            .header("Authorization", "Bearer #{jwtTokenRecipientAccount}")
            );

    private final ChainBuilder deleteInvitation =
            exec(
                    http("Delete Invitation")
                            .delete("/#{invitationId}")
                            .header("Authorization", "Bearer #{jwtTokenCompanyAccount}")
            );

    private final ChainBuilder getInvitationsReceivedByUser =
            exec(
                    http("Get Invitations Received By User")
                            .get("/user/received")
                            .header("Authorization", "Bearer #{jwtTokenRecipientAccount}")
            );

    private final ChainBuilder getInvitationsSentByCompany =
            exec(
                    http("Get Invitations Sent By Company")
                            .get("/company/#{companyId}/sent")
                            .header("Authorization", "Bearer #{jwtTokenCompanyAccount}")
            );

    private final ScenarioBuilder scenario = scenario("Invitation Operations")
            .exec(registerCompany)
            .exitHereIfFailed()
            // save company account information to remove this account at the end
            .exec(session -> session.setAll(
                    Map.of(
                            "companyAccountUsername", Objects.requireNonNull(session.get("username")),
                            "companyAccountPassword", Objects.requireNonNull(session.get("password"))
                    )
                )
            )
            // save information about companyId
            .exec(session -> session.set("senderCompanyId", session.get("companyId")))
            // get token of company owner
            .exec(authenticateByUsernameAndPassword("#{username}", "#{password}"))
            .exec(session -> session.set("jwtTokenCompanyAccount", session.get("jwtToken")))
            .exitHereIfFailed()
            .feed(userFeeder)
            // get new user from test users and save his id (username and password are overwritten)
            .exec(session -> session.set("recipientId", session.get("user_id")))
            .exec(invite)
            .exitHereIfFailed()
            // get token of test user to read his invitations
            .exec(authenticateByUsernameAndPassword("#{username}", "#{password}"))
            .exec(session -> session.set("jwtTokenRecipientAccount", session.get("jwtToken")))
            .exec(getInvitationsReceivedByUser)
            .exec(getInvitationsSentByCompany)
            .exec(session -> {
                boolean random = ThreadLocalRandom.current().nextBoolean();
                return session.set("whichOperation", random);
            })
            .doIfOrElse(session -> session.getBoolean("whichOperation"))
                .then(exec(acceptInvitation))
                .orElse(exec(declineInvitation))
            .exec(deleteInvitation)
            .exec(deleteUser("#{jwtTokenCompanyAccount}"));

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(USERS_AMOUNT).during(DURATION_SECONDS)
                )
        ).protocols(httpProtocol);
    }
}
