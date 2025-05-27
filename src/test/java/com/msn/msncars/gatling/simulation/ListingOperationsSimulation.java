package com.msn.msncars.gatling.simulation.listing;

import com.msn.msncars.car.CarOperationalStatus;
import com.msn.msncars.car.CarType;
import com.msn.msncars.car.CarUsage;
import com.msn.msncars.car.Fuel;
import com.msn.msncars.listing.OwnerType;
import com.msn.msncars.listing.ValidityPeriod;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpDsl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ListingOperationsSimulation extends Simulation {

    private final Random random = new Random();

    // RUNTIME PARAMS

    private static final int USER_COUNT = 50;
    private static final int DURATION_SECONDS = 30;

    // Feeder for test users data

    private final FeederBuilder<String> loginFeeder = CoreDsl.csv("./gatling/test_users.csv").circular();

    // HTTP Calls

    private final ChainBuilder getAllListings =
            exec(
                    http("Get all listings")
                            .get("http://localhost:8080/listings")
            );

    private final ChainBuilder authenticate =
            feed(loginFeeder)
            .exec(
                    http("User Login")
                            .post("http://localhost:8081/realms/MSNcars/protocol/openid-connect/token")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .formParam("grant_type", "password")
                            .formParam("client_id", "MSNcars")
                            .formParam("username", session -> session.getString("username"))
                            .formParam("password", session -> session.getString("password"))
                            .check(
                                    HttpDsl.status().is(200),
                                    CoreDsl.jsonPath("$.access_token").saveAs("jwtToken")
                            )
            );

    private final ChainBuilder createListing =
            feed(this::listingIterator)
            .exec(
                http("Create listing")
                            .post("http://localhost:8080/listings")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .header("Content-Type", "application/json")
                            .body(CoreDsl.StringBody("""
                                        {
                                            "ownerId": "#{user_id}",
                                            "ownerType": "#{ownerType}",
                                            "modelId": #{modelId},
                                            "featuresIds": #{featuresIds},
                                            "price": #{price},
                                            "productionYear": #{productionYear},
                                            "mileage": #{mileage},
                                            "fuel": "#{fuel}",
                                            "carUsage": "#{carUsage}",
                                            "carOperationalStatus": "#{carOperationalStatus}",
                                            "carType": "#{carType}",
                                            "description": "#{description}",
                                            "validityPeriod": "#{validityPeriod}"
                                        }
                                        """)
                            )
                        .check(HttpDsl.status().is(201))
                        .check(CoreDsl.bodyString().saveAs("listingId"))
            );

    private final ChainBuilder getLastPostedListing =
            exec(
                    http("Get last posted listing")
                            .get("http://localhost:8080/listings/#{listingId}")
            );

    private final ChainBuilder deleteLastPostedListing =
            exec(
                    http("Delete last posted listing")
                            .delete("http://localhost:8080/listings/#{listingId}")
                            .header("Authorization", "Bearer #{jwtToken}")
            );

    // Scenarios Definition

    private final ScenarioBuilder scn = CoreDsl.scenario("Listing Operations Load Test")
            .exec(getAllListings)
            .exec(authenticate)
            .exitHereIfFailed()
            .exec(createListing)
            .exitHereIfFailed()
            .exec(getLastPostedListing)
            .exec(deleteLastPostedListing);

    // Load Simulation

    {
        setUp(
                scn.injectOpen(
                    constantUsersPerSec(USER_COUNT).during(DURATION_SECONDS)
                )
        ).assertions(
                global().responseTime().percentile(95).lt(1000),
                global().successfulRequests().percent().is(100.),
                forAll().responseTime().max().lt(5000)
        );
    }

    // Helper methods

    private Map<String, Object> generateListing() {
        Map<String, Object> listings = new HashMap<>();
        listings.put("ownerType", OwnerType.USER.toString());
        listings.put("modelId", random.nextLong(1684, 1695)); // right now we are fetching models with those ids
        listings.put("featuresIds", new ArrayList<>());
        listings.put("price", random.nextLong(1, 100_000));
        listings.put("productionYear", random.nextInt(1900, LocalDateTime.now().getYear()));
        listings.put("mileage", random.nextLong(0, 500_000));
        listings.put("fuel", randomEnumValue(Fuel.class));
        listings.put("carUsage", randomEnumValue(CarUsage.class));
        listings.put("carOperationalStatus", randomEnumValue(CarOperationalStatus.class));
        listings.put("carType", randomEnumValue(CarType.class));
        listings.put("description", "");
        listings.put("validityPeriod", randomEnumValue(ValidityPeriod.class));
        return listings;
    }

    private<T extends Enum<?>> T randomEnumValue(Class<T> enumClass) {
        T[] allEnums = enumClass.getEnumConstants();
        return allEnums[random.nextInt(allEnums.length)];
    }

    private Iterator<Map<String, Object>> listingIterator() {
        return Stream.generate(this::generateListing)
                .iterator();
    }
}
