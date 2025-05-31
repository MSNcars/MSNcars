package com.msn.msncars.gatling.simulation;

import com.msn.msncars.gatling.scenario.ListingScenarios;
import com.msn.msncars.gatling.scenario.UserScenarios;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ListingOperationsSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http
                    .baseUrl("http://localhost:80");

    private static final int USER_COUNT = Integer.parseInt(System.getProperty("users", "50"));
    private static final int DURATION_SECONDS = Integer.parseInt(System.getProperty("duration", "30"));

    private final ChainBuilder getAllListings =
            exec(
                    http("Get all listings")
                            .get("/listings")
            );

    private final ChainBuilder authenticate = UserScenarios.authenticate;

    private final ChainBuilder createListing = ListingScenarios.createListing;

    private final ChainBuilder getLastPostedListing =
            exec(
                    http("Get last posted listing")
                            .get("/listings/#{listingId}")
            );

    private final ChainBuilder deleteLastPostedListing = ListingScenarios.deleteListing;

    private final ScenarioBuilder scn = CoreDsl.scenario("Listing Operations Load Test")
            .exec(getAllListings)
            .exec(authenticate)
            .exitHereIfFailed()
            .exec(createListing)
            .exitHereIfFailed()
            .exec(getLastPostedListing)
            .exec(deleteLastPostedListing);

    {
        setUp(
                scn.injectOpen(
                        rampUsers(USER_COUNT).during(DURATION_SECONDS)
                )
        ).protocols(httpProtocol);
    }
}
