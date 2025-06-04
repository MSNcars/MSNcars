package com.msn.msncars.gatling.scenario;

import com.msn.msncars.car.CarOperationalStatus;
import com.msn.msncars.car.CarType;
import com.msn.msncars.car.CarUsage;
import com.msn.msncars.car.Fuel;
import com.msn.msncars.listing.OwnerType;
import com.msn.msncars.listing.ValidityPeriod;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.http.HttpDsl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ListingScenarios {

    private static final Random random = new Random();

    private static final ChainBuilder getAllMakes =
            exec(
                    http("Get All Makes")
                            .get("/make/all")
                            .check(jsonPath("$[*].name").findAll().saveAs("makeNames"))
            );

    private static final ChainBuilder getMakeInfo =
            exec(
                    http("Get Make Info")
                            .get("/make/#{makeName}")
                            .check(CoreDsl.jsonPath("$.models[*].id").findAll().saveAs("modelIds"))
            );

    public static final ChainBuilder createListing =
            // get all makes to simulate user selecting one make from all possibilities
            exec(getAllMakes)
            .exec(session -> {
                List<String> makeNames = session.getList("makeNames");

                if (makeNames.isEmpty())
                    throw new RuntimeException("No make names found in response.");

                return session.set("makeName", randomListElement(makeNames));
            })
            .exitHereIfFailed()
            // fetch information about models of this make and select one of the models
            .exec(getMakeInfo)
            .exec(session -> {
                List<String> modelIds = session.getList("modelIds");

                if (modelIds.isEmpty())
                    throw new RuntimeException("No model IDs found in response.");

                return session.set("modelId", randomListElement(modelIds));
            })
            .exitHereIfFailed()
            // create listing
            .feed(listingIterator())
                    .exec(
                            http("Create listing")
                                    .post("/listings")
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

    public static final ChainBuilder deleteListing =
            exec(
                    http("Delete listing")
                            .delete("/listings/#{listingId}")
                            .header("Authorization", "Bearer #{jwtToken}")
            );

    private static Map<String, Object> generateListing() {
        Map<String, Object> listings = new HashMap<>();
        listings.put("ownerType", OwnerType.USER.toString());
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

    private static <T extends Enum<?>> T randomEnumValue(Class<T> enumClass) {
        T[] allEnums = enumClass.getEnumConstants();
        return allEnums[random.nextInt(allEnums.length)];
    }

    private static String randomListElement(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }

    private static Iterator<Map<String, Object>> listingIterator() {
        return Stream.generate(ListingScenarios::generateListing)
                .iterator();
    }
}
