package com.msn.msncars.gatling.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static com.msn.msncars.gatling.scenario.CompanyRegistrationScenario.registerCompany;
import static com.msn.msncars.gatling.scenario.UserScenarios.deleteUser;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CompanyOperationsSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http
                    .baseUrl("http://localhost:80/company");

    private static final int USERS_AMOUNT = Integer.parseInt(System.getProperty("users", "50"));
    private static final int DURATION_SECONDS = Integer.parseInt(System.getProperty("duration", "30"));

    private final ChainBuilder getCompanyInfo =
            exec(
                    http("Get Company Info")
                            .get("/#{companyId}")
            );

    private final ChainBuilder getCompanyMembers =
            exec(
                    http("Get Company Members")
                            .get("/#{companyId}/members")
            );

    private final ChainBuilder getCompanyOwner =
            exec(
                    http("Get Company Owner")
                            .get("/#{companyId}/owner")
            );

    private final ChainBuilder getCompaniesUserBelongsTo =
            exec(
                    http("Get Companies User Belongs To")
                            .get("/by-user/#{userId}")
            );

    private final ScenarioBuilder scenario = scenario("Company Operations Simulation")
            .exec(registerCompany)
            .exitHereIfFailed()
            .exec(getCompanyInfo)
            .exec(getCompanyMembers)
            .exec(getCompanyOwner)
            .exec(getCompaniesUserBelongsTo)
            .exec(deleteUser("#{username}", "#{password}"));

    {
        setUp(
            scenario.injectOpen(
                    rampUsers(USERS_AMOUNT).during(DURATION_SECONDS)
            )
        ).protocols(httpProtocol);
    }
}
