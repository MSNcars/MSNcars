package com.msn.msncars.listing;

import com.msn.msncars.car.*;
import com.msn.msncars.car.make.Make;
import com.msn.msncars.car.make.MakeRepository;
import com.msn.msncars.car.model.Model;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
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
    private DataSource dataSource;


    @BeforeEach
    public void setUp() {
        listingRepository.deleteAll();
    }

    @Test
    public void getAllListings_ShouldReturnAllListings_And200Code() throws Exception {
        // given

        Make toyota = new Make(1L, "Toyota");
        Make bmw = new Make(2L, "BMW");
        Make ford = new Make(3L, "Ford");

        Model corolla = new Model(1L, "Corolla", toyota);
        Model series3 = new Model(2L, "3 Series", bmw);
        Model focus = new Model(3L, "Focus", ford);

        Company autoWorld = new Company(null,
                "1",
                "Auto World",
                "123 Main St",
                "123-456-789",
                "contact@autoworld.com");
        Company bmwCenter = new Company(null,
                "2",
                "BMW Center",
                "456 BMW Rd",
                "987-654-321",
                "sales@bmwcenter.com");
        Company fordDealer = new Company(null,
                "3",
                "Ford Dealer",
                "789 Ford Ln",
                "555-222-111",
                "info@forddealer.com");

        Feature sunroof = new Feature(null, "Sunroof");
        Feature navigation = new Feature(null, "Navigation");
        Feature leatherSeats = new Feature(null, "Leather Seats");

        Listing listing1 = new Listing(
                null,
                "1",
                autoWorld,
                corolla,
                List.of(sunroof, navigation),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusMonths(1),
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
                "2",
                bmwCenter,
                series3,
                List.of(leatherSeats),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusMonths(2),
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
                "3",
                fordDealer,
                focus,
                List.of(navigation),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusWeeks(3),
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

        makeRepository.save(toyota);
        makeRepository.save(bmw);
        makeRepository.save(ford);

        modelRepository.save(corolla);
        modelRepository.save(series3);
        modelRepository.save(focus);

        companyRepository.save(autoWorld);
        companyRepository.save(bmwCenter);
        companyRepository.save(fordDealer);

        featureRepository.save(sunroof);
        featureRepository.save(navigation);
        featureRepository.save(leatherSeats);

        listingRepository.save(listing1);
        listingRepository.save(listing2);
        listingRepository.save(listing3);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings")
                .contentType(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].model.name", is("Corolla")))
                .andExpect(jsonPath("$[1].price", is(32000.00)))
                .andExpect(jsonPath("$[2].fuel", is("PETROL")));
    }

    @Test
    public void getListingById_ShouldReturnListing_And200Code_WhenRequestedListingExists() throws Exception {
        Make toyota = new Make(1L, "Toyota");

        Model corolla = new Model(1L, "Corolla", toyota);

        Company autoWorld = new Company(null,
                "1",
                "Auto World",
                "123 Main St",
                "123-456-789",
                "contact@autoworld.com");

        Feature sunroof = new Feature(null, "Sunroof");
        Feature navigation = new Feature(null, "Navigation");

        Listing listing1 = new Listing(
                null,
                "1",
                autoWorld,
                corolla,
                List.of(sunroof, navigation),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusMonths(1),
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

        makeRepository.save(toyota);
        modelRepository.save(corolla);
        companyRepository.save(autoWorld);
        featureRepository.save(sunroof);
        featureRepository.save(navigation);
        Listing savedListing = listingRepository.save(listing1);
        Long listingId = savedListing.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        // when & then

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.sellingCompany.name", is("Auto World")))
                .andExpect(jsonPath("$.productionYear", is(2020)))
                .andExpect(jsonPath("$.carType", is("SEDAN")));
    }

    @Test
    public void getListingById_ShouldReturn404Code_AndAccordingMessage_WhenListingDoesNotExist() throws Exception {
        // given

        Long listingId = 2L;

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

        Make toyota = new Make(1L, "Toyota");
        Make bmw = new Make(2L, "BMW");
        Make ford = new Make(3L, "Ford");

        Model corolla = new Model(1L, "Corolla", toyota);
        Model series3 = new Model(2L, "3 Series", bmw);
        Model focus = new Model(3L, "Focus", ford);

        Company autoWorld = new Company(null,
                "1",
                "Auto World",
                "123 Main St",
                "123-456-789",
                "contact@autoworld.com");
        Company bmwCenter = new Company(null,
                "2",
                "BMW Center",
                "456 BMW Rd",
                "987-654-321",
                "sales@bmwcenter.com");
        Company fordDealer = new Company(null,
                "3",
                "Ford Dealer",
                "789 Ford Ln",
                "555-222-111",
                "info@forddealer.com");

        Feature sunroof = new Feature(null, "Sunroof");
        Feature navigation = new Feature(null, "Navigation");
        Feature leatherSeats = new Feature(null, "Leather Seats");

        Listing listing1 = new Listing(
                null,
                "1",
                autoWorld,
                corolla,
                List.of(sunroof, navigation),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusMonths(1),
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
                "2",
                bmwCenter,
                series3,
                List.of(leatherSeats),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusMonths(2),
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
                "1",
                fordDealer,
                focus,
                List.of(navigation),
                ZonedDateTime.now(),
                ZonedDateTime.now().plusWeeks(3),
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

        makeRepository.save(toyota);
        makeRepository.save(bmw);
        makeRepository.save(ford);

        modelRepository.save(corolla);
        modelRepository.save(series3);
        modelRepository.save(focus);

        companyRepository.save(autoWorld);
        companyRepository.save(bmwCenter);
        companyRepository.save(fordDealer);

        featureRepository.save(sunroof);
        featureRepository.save(navigation);
        featureRepository.save(leatherSeats);

        listingRepository.save(listing1);
        listingRepository.save(listing2);
        listingRepository.save(listing3);

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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].model.name", is("Corolla")))
                .andExpect(jsonPath("$[1].price", is(14000.00)))
                .andExpect(jsonPath("$[1].fuel", is("PETROL")));
    }

    @AfterAll
    static void tearDown(@Autowired DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }

        postgres.stop();
    }

}

