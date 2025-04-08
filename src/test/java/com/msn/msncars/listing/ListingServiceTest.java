package com.msn.msncars.listing;

import com.msn.msncars.car.*;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingService listingService;

    @Test
    public void getAllListings_WhenListingsExist_ShouldReturnAllOfThem_AsCorrectListingResponseDTO() {
        // given

        Listing listing1 = new Listing(
                1L,
                "1",
                new Company(
                        1L,
                        "1",
                        "company1",
                        "address1",
                        "phone1",
                        "email1"
                ),
                new Make(1L, "Toyota"),
                new Model(1L, "Corolla"),
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

        Listing listing2 = new Listing(
                2L,
                "2",
                new Company(
                        2L,
                        "1",
                        "company2",
                        "address2",
                        "phone2",
                        "email2"
                ),
                new Make(1L, "Toyota"),
                new Model(2L, "Yaris"),
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

        Listing listing3 = new Listing(
                3L,
                "3",
                new Company(),
                new Make(2L, "Ford"),
                new Model(3L, "Focus"),
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

        List<Listing> listings = new ArrayList<>(Arrays.asList(listing1, listing2, listing3));
        Mockito.when(listingRepository.findAll()).thenReturn(listings);

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

        Mockito.when(listingMapper.fromListing(listing1)).thenReturn(listingResponse1);
        Mockito.when(listingMapper.fromListing(listing2)).thenReturn(listingResponse2);
        Mockito.when(listingMapper.fromListing(listing3)).thenReturn(listingResponse3);

        //when

        List<ListingResponse> listingResponses = listingService.getAllListings();

        //then

        assertEquals(3, listingResponses.size());
        assertEquals(listing1.getId(), listingResponses.getFirst().id());
        assertEquals(listing1.getModel().getName(), listingResponses.getFirst().modelName());
        assertEquals(listing1.getDescription(), listingResponses.getFirst().description());
        assertEquals(listing2.getMake().getName(), listingResponses.get(1).makeName());
        assertEquals(listing2.getSellingCompany().getName(), listingResponses.get(1).sellingCompanyName());
        assertEquals(listing3.getSellingCompany().getName(), listingResponses.get(2).sellingCompanyName());
        assertEquals(listing3.getMileage(), listingResponses.get(2).mileage());

    }

}
