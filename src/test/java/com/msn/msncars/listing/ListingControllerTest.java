package com.msn.msncars.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msn.msncars.car.*;
import com.msn.msncars.exception.GlobalExceptionHandler;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
@Import(GlobalExceptionHandler.class)
public class ListingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private ListingService listingService;

    @Test
    public void getAllListings_ShouldReturnAllListings() throws Exception {
        // given

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

        // when & then

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].modelName", is("Corolla")))
                .andExpect(jsonPath("$[1].price", is(35000.00)))
                .andExpect(jsonPath("$[2].fuel", is("DIESEL")));

    }

    @Test
    public void getListingById_ShouldReturnListing_WhenListingExists() throws Exception {
        // given

        Long listingId = 1L;

        ListingResponse listingResponse = new ListingResponse(
                listingId,
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

        Mockito.when(listingService.getListingById(listingId)).thenReturn(listingResponse);

        // when & then

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.sellingCompanyName", is("company1")))
                .andExpect(jsonPath("$.productionYear", is(2020)))
                .andExpect(jsonPath("$.carType", is("SEDAN")));
    }

    @Test
    public void getListingById_ShouldReturnNotFoundStatus_WhenListingDoesNotExist() throws Exception {
        // given

        Long listingId = 2L;

        Mockito.when(listingService.getListingById(listingId))
                .thenThrow(new ListingNotFoundException("Listing not found with id: " + listingId));

        // when & then

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/listings/" + listingId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Listing not found with id: 2"));
    }

    @Test
    public void createListing_ShouldReturnWantedResponse_WhenRequestIsValid() throws Exception {
        // given

        ListingRequest listingRequest = new ListingRequest(
                "1",
                null,
                1L,
                1L,
                new ArrayList<>(Arrays.asList(1L, 2L)),
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

        Long expectedId = 1L;

        Mockito.when(listingService.createListing(listingRequest)).thenReturn(expectedId);

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        // when & then

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/listings")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/listings/" + expectedId))
                .andExpect(content().string(expectedId.toString()));
    }

    @Test
    public void createListing_ShouldReturnErrorStatus_WhenRequestIsInvalid() throws Exception {
        // given

        ListingRequest listingRequest = new ListingRequest(
                "1",
                null,
                1L,
                1L,
                new ArrayList<>(Arrays.asList(1L, 2L)),
                LocalDate.now().plusDays(35),
                false,
                new BigDecimal("35000.00"),
                1021,
                15000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.COUPE,
                "desc2"
        );

        String requestJson = objectMapper.writeValueAsString(listingRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/listings")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }


}
