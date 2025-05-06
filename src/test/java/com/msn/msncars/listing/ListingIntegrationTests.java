package com.msn.msncars.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msn.msncars.car.*;
import com.msn.msncars.car.make.Make;
import com.msn.msncars.car.make.MakeRepository;
import com.msn.msncars.car.model.Model;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.zaxxer.hikari.HikariDataSource;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListingIntegrationTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    MakeRepository makeRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    FeatureRepository featureRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    Clock clock;

    Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T23:00:00Z"), ZoneId.of("UTC"));

    public ListingIntegrationTests() {
        if (!postgres.isRunning()) {
            postgres.start();
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.when(clock.instant()).thenReturn(fixedClock.instant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @BeforeAll
    public void setUpBeforeAll() {
        initializeSharedTestData();
    }

    @AfterEach
    public void cleanUp() {
        listingRepository.deleteAll();
    }

    @AfterAll
    static void tearDown(@Autowired DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }

        postgres.stop();
    }

    private void initializeSharedTestData() {
        Make toyota = new Make(1L, "Toyota");
        Make bmw = new Make(2L, "BMW");
        Make ford = new Make(3L, "Ford");

        makeRepository.save(toyota);
        makeRepository.save(bmw);
        makeRepository.save(ford);

        Model corolla = new Model(1L, "Corolla", toyota);
        Model series3 = new Model(2L, "3 Series", bmw);
        Model focus = new Model(3L, "Focus", ford);

        modelRepository.save(corolla);
        modelRepository.save(series3);
        modelRepository.save(focus);

        Company autoWorld = new Company(null,
                "1",
                "Auto World",
                "123 Main St",
                "123-456-789",
                "contact@autoworld.com");
        autoWorld.addMember("1");
        Company bmwCenter = new Company(null,
                "2",
                "BMW Center",
                "456 BMW Rd",
                "987-654-321",
                "sales@bmwcenter.com");
        bmwCenter.addMember("2");
        Company fordDealer = new Company(null,
                "3",
                "Ford Dealer",
                "789 Ford Ln",
                "555-222-111",
                "info@forddealer.com");
        fordDealer.addMember("3");

        companyRepository.save(autoWorld);
        companyRepository.save(bmwCenter);
        companyRepository.save(fordDealer);

        Feature sunroof = new Feature(null, "Sunroof");
        Feature navigation = new Feature(null, "Navigation");
        Feature leatherSeats = new Feature(null, "Leather Seats");

        featureRepository.save(sunroof);
        featureRepository.save(navigation);
        featureRepository.save(leatherSeats);
    }

    private class TestContext{
        Make toyota;
        Make bmw;
        Make ford;

        Model corolla;
        Model series3;
        Model focus;

        Company autoWorld;
        Company bmwCenter;
        Company fordDealer;

        Feature sunroof;
        Feature navigation;
        Feature leatherSeats;
    }

    private TestContext getTestContext(){
        TestContext testContext = new TestContext();

        testContext.toyota = makeRepository.findByName("Toyota")
                .orElseThrow(() -> new RuntimeException("Make 'Toyota' not found (should've been added in tests set up data"));
        testContext.bmw = makeRepository.findByName("BMW")
                .orElseThrow(() -> new RuntimeException("Make 'BMW' not found (should've been added in tests set up data"));
        testContext.ford = makeRepository.findByName("Ford")
                .orElseThrow(() -> new RuntimeException("Make 'Ford' not found (should've been added in tests set up data"));


        testContext.corolla = modelRepository.findByName("Corolla")
                .orElseThrow(() -> new RuntimeException("Model 'Corolla' not found (should've been added in tests set up data"));
        testContext.series3 = modelRepository.findByName("3 Series")
                .orElseThrow(() -> new RuntimeException("Model '3 Series' not found (should've been added in tests set up data"));
        testContext.focus = modelRepository.findByName("Focus")
                .orElseThrow(() -> new RuntimeException("Model 'Focus' not found (should've been added in tests set up data"));

        testContext.sunroof = featureRepository.findByName("Sunroof")
                .orElseThrow(() -> new RuntimeException("Feature 'Sunroof' not found (should've been added in tests set up data"));
        testContext.navigation = featureRepository.findByName("Navigation")
                .orElseThrow(() -> new RuntimeException("Feature 'Navigation' not found (should've been added in tests set up data"));
        testContext.leatherSeats = featureRepository.findByName("Leather Seats")
                .orElseThrow(() -> new RuntimeException("Feature 'Leather Seats' not found (should've been added in tests set up data"));

        testContext.autoWorld = companyRepository.findByName("Auto World")
                .orElseThrow(() -> new RuntimeException("Company 'Auto World' not found (should've been added in tests set up data"));
        testContext.bmwCenter = companyRepository.findByName("BMW Center")
                .orElseThrow(() -> new RuntimeException("Company 'BMW Center' not found (should've been added in tests set up data"));
        testContext.fordDealer = companyRepository.findByName("Ford Dealer")
                .orElseThrow(() -> new RuntimeException("Company 'Ford Dealer' not found (should've been added in tests set up data"));

        return testContext;
    }

    @Test
    public void getAllListingsWithoutFilters_ShouldReturnAllListings_And200Code() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listing1 = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing listing2 = new Listing(
                null,
                testContext.bmwCenter.getId().toString(),
                OwnerType.COMPANY,
                testContext.series3,
                List.of(testContext.leatherSeats),
                fixedClock.instant(),
                fixedClock.instant().plus(61, ChronoUnit.DAYS),
                false,
                new BigDecimal("32000.00"),
                2022,
                15000,
                Fuel.DIESEL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Luxury BMW 3 Series with leather interior."
        );

        Listing listing3 = new Listing(
                null,
                testContext.fordDealer.getId().toString(),
                OwnerType.COMPANY,
                testContext.focus,
                List.of(testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(21, ChronoUnit.DAYS),
                false,
                new BigDecimal("14000.00"),
                2018,
                78000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.DAMAGED,
                CarType.HATCHBACK,
                "Reliable Ford Focus, ideal for city use."
        );

        listingRepository.save(listing1);
        listingRepository.save(listing2);
        listingRepository.save(listing3);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].model.name", is("Corolla")))
                // can you get correct make name
                .andExpect(jsonPath("$[0].model.make.name", is("Toyota")))
                .andExpect(jsonPath("$[1].price", is(32000.00)))
                .andExpect(jsonPath("$[2].fuel", is("PETROL")));
    }

    @Test
    public void getAllListingsWithFilters_ShouldReturnFilteredListings_And200Code() throws Exception {
        // given
        var testContext = getTestContext();

        Model yaris = new Model(4L, "Yaris", testContext.toyota);
        modelRepository.save(yaris);

        Listing listing1 = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing listing2 = new Listing(
                null,
                testContext.bmwCenter.getId().toString(),
                OwnerType.COMPANY,
                testContext.series3,
                List.of(testContext.leatherSeats),
                fixedClock.instant(),
                fixedClock.instant().plus(61, ChronoUnit.DAYS),
                false,
                new BigDecimal("32000.00"),
                2022,
                15000,
                Fuel.DIESEL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Luxury BMW 3 Series with leather interior."
        );

        Listing listing3 = new Listing(
                null,
                testContext.fordDealer.getId().toString(),
                OwnerType.COMPANY,
                testContext.focus,
                List.of(testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(21, ChronoUnit.DAYS),
                false,
                new BigDecimal("14000.00"),
                2018,
                78000,
                Fuel.DIESEL,
                CarUsage.USED,
                CarOperationalStatus.DAMAGED,
                CarType.HATCHBACK,
                "Reliable Ford Focus, ideal for city use."
        );

        Listing listing4 = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                yaris,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("12000.00"),
                2020,
                47000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota with sunroof and nav."
        );

        Listing listing5 = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                yaris,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("10000.00"),
                2020,
                41000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota with sunroof and nav."
        );

        listingRepository.save(listing1);
        listingRepository.save(listing2);
        listingRepository.save(listing3);
        listingRepository.save(listing4);
        listingRepository.save(listing5);

        MockHttpServletRequestBuilder request1 = MockMvcRequestBuilders
                .get("/listings?makeName=Toyota&sortAttribute=PRICE&sortOrder=ASCENDING")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletRequestBuilder request2 = MockMvcRequestBuilders
                .get("/listings?fuel=DIESEL&sortAttribute=MILEAGE&sortOrder=DESCENDING")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].model.name", is("Yaris")))
                .andExpect(jsonPath("$[0].model.make.name", is("Toyota")))
                .andExpect(jsonPath("$[0].price", is(10000.00)))
                .andExpect(jsonPath("$[1].model.name", is("Yaris")))
                .andExpect(jsonPath("$[1].model.make.name", is("Toyota")))
                .andExpect(jsonPath("$[1].price", is(12000.00)))
                .andExpect(jsonPath("$[2].model.name", is("Corolla")))
                .andExpect(jsonPath("$[2].model.make.name", is("Toyota")))
                .andExpect(jsonPath("$[2].price", is(18000.00)));

        mockMvc.perform(request2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].model.name", is("Focus")))
                .andExpect(jsonPath("$[0].model.make.name", is("Ford")))
                .andExpect(jsonPath("$[0].fuel", is("DIESEL")))
                .andExpect(jsonPath("$[0].mileage", is(78000)))
                .andExpect(jsonPath("$[1].model.name", is("3 Series")))
                .andExpect(jsonPath("$[1].model.make.name", is("BMW")))
                .andExpect(jsonPath("$[1].fuel", is("DIESEL")))
                .andExpect(jsonPath("$[1].mileage", is(15000)));

    }

    @Test
    public void getListingById_ShouldReturnListing_And200Code_WhenRequestedListingExists() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listing = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listing);
        Long listingId = savedListing.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.ownerId", is("1")))
                .andExpect(jsonPath("$.ownerType", is("COMPANY")))
                .andExpect(jsonPath("$.productionYear", is(2020)))
                .andExpect(jsonPath("$.carType", is("SEDAN")));
    }

    @Test
    public void getListingById_ShouldReturn404Code_AndAccordingMessage_WhenListingDoesNotExist() throws Exception {
        // given

        long listingId = 2L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Listing not found with id: 2"));
    }

    @Test
    public void getMyListings_ShouldReturn200Code_AndReturnListingsOfSpecifiedUser() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listing1 = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing listing2 = new Listing(
                null,
                "1",
                OwnerType.USER,
                testContext.focus,
                List.of(testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(21, ChronoUnit.DAYS),
                false,
                new BigDecimal("14000.00"),
                2018,
                78000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.DAMAGED,
                CarType.HATCHBACK,
                "Reliable Ford Focus, ideal for city use."
        );

        listingRepository.save(listing1);
        listingRepository.save(listing2);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/me")
                .with(jwt().jwt(jwt))
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model.name", is("Focus")))
                .andExpect(jsonPath("$[0].price", is(14000.00)))
                .andExpect(jsonPath("$[0].fuel", is("PETROL")));
    }

    @Test
    public void createListing_ShouldReturn201Code_AndIdOfCreatedResource_AndShouldCreateNewListingInDatabase_WhenRequestIsValid()
            throws Exception {
        // given
        var testContext = getTestContext();

        ListingRequest listingRequest = new ListingRequest(
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                1L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/listings")
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, Matchers.startsWith("/listings/")))
                .andReturn();

        assertEquals(1, listingRepository.count());

        String content = result.getResponse().getContentAsString();
        Long generatedId = objectMapper.readValue(content, Long.class);

        assertTrue(listingRepository.findById(generatedId).isPresent());

        Optional<Listing> saved = listingRepository.findById(generatedId);

        assertTrue(saved.isPresent());
        assertEquals(45000, saved.get().getMileage());
        assertEquals(new BigDecimal("18000.00"), saved.get().getPrice());
    }

    @Test
    public void createListing_ShouldReturn400Code_AndAccordingMessage_WhenRequestIsInvalid() throws Exception {
        // given
        var testContext = getTestContext();

        ListingRequest listingRequest = new ListingRequest(
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                1L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("18000.00"),
                1820,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/listings")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errors[0].field", is("productionYear")))
                .andExpect(jsonPath("$.errors[0].message", is("must be greater than or equal to 1900")));
    }

    @Test
    public void updateListing_ShouldReturnUpdatedListing_And200Code_AndShouldUpdateListingInDatabase_WhenRequestIsValid() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        // want to update model and price
        ListingRequest listingUpdateRequest = new ListingRequest(
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                2L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("12000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingUpdateRequest);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/listings/" + listingInDatabaseId)
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(listingInDatabaseId.intValue())))
                .andExpect(jsonPath("$.model.id", is(2)))
                .andExpect(jsonPath("$.price", is(12000.00)));

        Optional<Listing> saved = listingRepository.findById(listingInDatabaseId);

        assertTrue(saved.isPresent());
        assertEquals(2L, saved.get().getModel().getId());
        assertEquals(new BigDecimal("12000.00"), saved.get().getPrice());
    }

    @Test
    public void updateListing_ShouldReturn403Code_AndAccordingMessage_WhenUserWantsToUpdateNotHisListings() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                "1",
                OwnerType.USER,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        // even when sending new owner id in request
        ListingRequest listingUpdateRequest = new ListingRequest(
                "2",
                OwnerType.USER,
                2L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("12000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingUpdateRequest);

        String userId = "2";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/listings/" + listingInDatabaseId)
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().string("You don't have permission to this listing."));
    }

    @Test
    public void updateListing_ShouldReturn400Code_AndAccordingMessage_WhenRequestIsInvalid() throws Exception {
        // given
        var testContext = getTestContext();

        ListingRequest listingRequest = new ListingRequest(
                "1",
                OwnerType.USER,
                1L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("-18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        long listingId = 1L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/listings/" + listingId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errors[0].field", is("price")))
                .andExpect(jsonPath("$.errors[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    public void updateListing_ShouldReturn404Code_AndAccordingMessage_WhenListingDoesNotExist() throws Exception {
        // given
        var testContext = getTestContext();

        ListingRequest listingRequest = new ListingRequest(
                "1",
                OwnerType.USER,
                1L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        long listingId = 1L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/listings/" + listingId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Listing not found with id: 1"));
    }

    @Test
    public void updateListing_ShouldReturn400Code_AndAccordingMessage_WhenListingIsRevoked() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                true,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        // want to update model and price
        ListingRequest listingUpdateRequest = new ListingRequest(
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                2L,
                List.of(testContext.sunroof.getId(), testContext.navigation.getId()),
                new BigDecimal("12000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav.",
                ValidityPeriod.STANDARD
        );

        String requestJson = objectMapper.writeValueAsString(listingUpdateRequest);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/listings/" + listingInDatabaseId)
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot modify revoked listing."));

    }

    @Test
    public void extendExpirationDate_ShouldReturn200Code_AndListingWithUpdatedDate_AndUpdateListingInDatabase_WhenRequestIsValid () throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(2, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        ValidityPeriod validityPeriod = ValidityPeriod.EXTENDED;

        String requestJson = objectMapper.writeValueAsString(validityPeriod);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/listings/" + listingInDatabaseId + "/extend")
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(listingInDatabaseId.intValue())));

        Optional<Listing> saved = listingRepository.findById(listingInDatabaseId);

        assertTrue(saved.isPresent());
        // one second tolerance
        Duration diff = Duration.between(fixedClock.instant().plus(32, ChronoUnit.DAYS), saved.get().getExpiresAt());
        assertTrue(diff.isZero());
    }

    @Test
    public void extendExpirationDate_ShouldReturn403Code_AndAccordingMessage_WhenUserWantsToExtendNotHisListings() throws Exception
    {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(2, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        ValidityPeriod validityPeriod = ValidityPeriod.EXTENDED;

        String requestJson = objectMapper.writeValueAsString(validityPeriod);

        String userId = "2";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/listings/" + listingInDatabaseId + "/extend")
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().string("You don't have permission to this listing."));
    }

    @Test
    public void extendExpirationDate_ShouldReturn404Code_AndAccordingMessage_WhenListingDoesNotExist() throws Exception {
        // given

        ValidityPeriod validityPeriod = ValidityPeriod.EXTENDED;

        String requestJson = objectMapper.writeValueAsString(validityPeriod);

        long listingId = 1L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/listings/" + listingId + "/extend")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Listing not found with id: 1"));
    }

    @Test
    public void extendExpirationDate_ShouldReturn400Code_AndAccordingMessage_WhenListingIsRevoked() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(2, ChronoUnit.DAYS),
                true,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        ValidityPeriod validityPeriod = ValidityPeriod.EXTENDED;

        String requestJson = objectMapper.writeValueAsString(validityPeriod);

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/listings/" + listingInDatabaseId + "/extend")
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot extend revoked listing."));
    }

    @Test
    public void setListingRevokedStatus_ShouldReturn200Code_AndListingWithUpdatedRevokedStatus_AndUpdateListingInDatabase_WhenRequestIsValid () throws Exception {
        // given
        var testContext = getTestContext();

        Listing listingInDatabase = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(2, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listingInDatabase);
        Long listingInDatabaseId = savedListing.getId();

        boolean isRevoked = true;

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/listings/" + listingInDatabaseId + "/set-revoked/" + isRevoked)
                .with(jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(listingInDatabaseId.intValue())))
                .andExpect(jsonPath("$.revoked", is(true)));

        Optional<Listing> saved = listingRepository.findById(listingInDatabaseId);

        assertTrue(saved.isPresent());
        assertTrue(saved.get().getRevoked());
    }

    @Test
    public void deleteListing_ShouldReturn200Code_AndDeleteListingFromDatabase_WhenListingExists() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listing = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listing);
        Long listingId = savedListing.getId();

        String userId = "1";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/listings/" + listingId)
                .with(jwt().jwt(jwt))
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertFalse(listingRepository.findById(listingId).isPresent());
    }

    @Test
    public void deleteListing_ShouldReturn403Code_WhenUserWantsToDeleteNotHisListing() throws Exception {
        // given
        var testContext = getTestContext();

        Listing listing = new Listing(
                null,
                testContext.autoWorld.getId().toString(),
                OwnerType.COMPANY,
                testContext.corolla,
                List.of(testContext.sunroof, testContext.navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        Listing savedListing = listingRepository.save(listing);
        Long listingId = savedListing.getId();

        String userId = "2";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/listings/" + listingId)
                .with(jwt().jwt(jwt))
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().string("You don't have permission to this listing."));
    }

    @Test
    public void deleteListing_ShouldReturn404Code_WhenListingDoesNotExist() throws Exception
    {
        // given

        long listingId = 1L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Listing not found with id: 1"));
    }
}
