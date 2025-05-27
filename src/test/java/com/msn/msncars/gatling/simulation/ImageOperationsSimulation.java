package com.msn.msncars.gatling.simulation;

import com.msn.msncars.gatling.scenario.ListingScenarios;
import com.msn.msncars.gatling.scenario.UserScenarios;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ImageOperationsSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http
                    .baseUrl("http://localhost:80");

    private static final int USERS_COUNT = Integer.parseInt(System.getProperty("users", "50"));
    private static final int DURATION_SECONDS = Integer.parseInt(System.getProperty("duration", "30"));

    private final ChainBuilder authenticate = UserScenarios.authenticate;

    private final ChainBuilder createListing = ListingScenarios.createListing;

    private final ChainBuilder attachImage =
            feed(imagesFeeder())
                .exec(
                        http("Attach image")
                                .post("/images")
                                .header("Authorization", "Bearer #{jwtToken}")
                                .formParam("listingId", "#{listingId}")
                                .formUpload("image", "#{imagePath}")
                                .asMultipartForm()
                                .check(HttpDsl.status().is(201))
                );

    private final ChainBuilder fetchLastListingImage =
            exec(session -> {
                List<String> imagesPaths = session.getList("imagesPaths");
                String firstImagePath = imagesPaths.getFirst();
                return session.set("listingImagePath", firstImagePath);
            })
            .exec(
                http("Fetch image")
                        .get("/images")
                        .header("Content-Type", "application/json")
                        .body(StringBody("""
                                {
                                    "path": "#{listingImagePath}"
                                }
                                """)
                        )
                        .check(status().is(200))
            );

    private final ChainBuilder fetchLastListingImagesPaths =
            exec(
                    http("Fetch last listing images paths")
                            .get("/listings/#{listingId}/images")
                            .check(
                                    jsonPath("$[*]").findAll().saveAs("imagesPaths")
                            )
            );

    private final ChainBuilder deleteLastPostedListing = ListingScenarios.deleteListing;

    private final ScenarioBuilder scenario = CoreDsl.scenario("Image Operations Load Test")
            .exec(authenticate)
            .exitHereIfFailed()
            .exec(createListing)
            .exitHereIfFailed()
            .exec(attachImage)
            .exitHereIfFailed()
            .exec(fetchLastListingImagesPaths)
            .exec(fetchLastListingImage)
            .exec(deleteLastPostedListing);

    {
        setUp(
            scenario.injectOpen(
                    rampUsers(USERS_COUNT).during(DURATION_SECONDS)
            )
        ).protocols(httpProtocol);
    }

    private FeederBuilder<Object> imagesFeeder() {
        File directory = new File("./src/test/resources/images/");

        if (!directory.exists())
            throw new IllegalStateException(String.format("File %s does not exist", directory.getAbsolutePath()));
        if (!directory.isDirectory())
            throw new IllegalStateException(String.format("File %s is not a directory", directory.getAbsolutePath()));

        String basePath = new File("./src/test/resources").getAbsolutePath();

        List<Map<String, Object>> imagesPaths = Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(File::isFile)
                .filter(file -> !file.getName().endsWith(".bmp"))
                .map(file -> {
                    String fullPath = file.getAbsolutePath();
                    String relativePath = fullPath.substring(basePath.length() + 1);
                    return Map.of("imagePath", (Object) relativePath);
                })
                .toList();

        return CoreDsl.listFeeder(imagesPaths).circular();
    }

}
