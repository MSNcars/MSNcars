package com.msn.msncars.listings;

import com.msn.msncars.listing.ListingController;
import com.msn.msncars.listing.ListingRequest;
import com.msn.msncars.listing.ListingService;
import com.msn.msncars.car.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ListingsTests {
    private MockMvc mockMvc;

    @Mock
    private ListingService listingService;

    @InjectMocks
    private ListingController listingController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();
    }

    @Test
    void createListing() throws Exception {
        // Given
        ListingRequest listingRequest = new ListingRequest("owner", 1L, 1L, 1L, Collections.emptyList(),
                LocalDate.now().plusMonths(1), false, BigDecimal.TEN, 2021, 10000, Fuel.PETROL, CarUsage.NEW,
                CarOperationalStatus.WORKING, CarType.SEDAN, "description");
        when(listingService.createListing(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/listings")
                        .contentType("application/json")
                        .content("{\"ownerId\": \"owner\", \"sellingCompanyId\": 1, \"makeId\": 1, \"modelId\": 1, \"featuresIds\": [], \"expiresAt\": \"2025-12-31\", \"revoked\": false, \"price\": 10.0, \"productionYear\": 2021, \"mileage\": 10000, \"fuel\": \"GASOLINE\", \"carUsage\": \"PERSONAL\", \"carOperationalStatus\": \"OPERATIONAL\", \"carType\": \"SEDAN\", \"description\": \"description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}
