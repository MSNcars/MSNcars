package com.msn.msncars.listing;

import com.msn.msncars.car.*;
import com.msn.msncars.listing.DTO.ListingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ListingControllerTest {
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter objectWriter = objectMapper.writer();

    @Mock
    private ListingService listingService;

    @InjectMocks
    private ListingController listingController;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();
    }

    @Test
    public void getAllListings_ShouldReturnAllListings() throws Exception {
        //given

        ListingResponse listingResponse1 = new ListingResponse(
                1L,
                "1",
                "company1",
                "Toyota",
                "Corolla",
                new ArrayList<>(Arrays.asList(
                        new Feature(1L, "feature1"),
                        new Feature(2L, "feature2")
                )),
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(30),
                false,
                new BigDecimal("45000.00"),
                2020,
                15000,
                Fuel.PETROL,
                CarUsage.NEW,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "desc1"
        );

        ListingResponse listingResponse2 = new ListingResponse(
                2L,
                "2",
                "company2",
                "Toyota",
                "Yaris",
                null,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(35),
                false,
                new BigDecimal("35000.00"),
                2021,
                15000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.COUPE,
                "desc2"
        );

        ListingResponse listingResponse3 = new ListingResponse(
                3L,
                "3",
                null,
                "Ford",
                "Focus",
                new ArrayList<>(Arrays.asList(
                        new Feature(3L, "feature3"),
                        new Feature(4L, "feature4"),
                        new Feature(5L, "feature5")
                )),
                LocalDate.now().minusDays(15),
                LocalDate.now().plusDays(50),
                false,
                new BigDecimal("25000.00"),
                2020,
                15000,
                Fuel.DIESEL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.HATCHBACK,
                "desc3"
        );

        List<ListingResponse> listings = new ArrayList<>(Arrays.asList(listingResponse1, listingResponse2, listingResponse3));

        Mockito.when(listingService.getAllListings()).thenReturn(listings);

        //when & then

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get("/listings")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].modelName", is("Corolla")))
                .andExpect(jsonPath("$[1].price", is(35000.00)))
                .andExpect(jsonPath("$[2].fuel", is("DIESEL")));

    }
}
