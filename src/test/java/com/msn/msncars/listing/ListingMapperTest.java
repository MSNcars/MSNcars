package com.msn.msncars.listing;

import com.msn.msncars.car.*;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ListingMapperTest {
    @Mock
    private MakeRepository makeRepository;

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private ListingMapper listingMapper;

    @Test
    public void fromListing_ShouldCorrectlyConvert_ListingToListingResponseDTO() {
        // given

        Listing listing = new Listing(
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

        // when

        ListingResponse listingResponse = listingMapper.fromListing(listing);

        //then
        assertNotNull(listingResponse);
        assertEquals(listing.getId(), listingResponse.id());
        assertEquals(listing.getOwnerId(), listingResponse.ownerId());
        assertEquals(listing.getSellingCompany().getName(), listingResponse.sellingCompanyName());
        assertEquals(listing.getMake().getName(), listingResponse.makeName());
        assertEquals(listing.getModel().getName(), listingResponse.modelName());
        assertEquals(listing.getFeatures().getFirst().getName(), listingResponse.features().getFirst().getName());
        assertEquals(listing.getCreatedAt(), listingResponse.createdAt());
        assertEquals(listing.getExpiresAt(), listingResponse.expiresAt());
        assertEquals(listing.getRevoked(), listingResponse.revoked());
        assertEquals(listing.getPrice(), listingResponse.price());
        assertEquals(listing.getProductionYear(), listingResponse.productionYear());
        assertEquals(listing.getMileage(), listingResponse.mileage());
        assertEquals(listing.getFuel(), listingResponse.fuel());
        assertEquals(listing.getCarUsage(), listingResponse.carUsage());
        assertEquals(listing.getCarOperationalStatus(), listingResponse.carOperationalStatus());
        assertEquals(listing.getCarType(), listingResponse.carType());
        assertEquals(listing.getDescription(), listingResponse.description());
    }

    @Test
    public void fromListing_ShouldCorrectlyConvert_ListingWithNullValues_ToListingResponseDTO() {
        // given

        Listing listing = new Listing(
                2L,
                "2",
                null,
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

        // when

        ListingResponse listingResponse = listingMapper.fromListing(listing);

        // then

        assertNotNull(listingResponse);
        assertEquals(listing.getId(), listingResponse.id());
        assertEquals(listing.getOwnerId(), listingResponse.ownerId());
        assertNull(listingResponse.sellingCompanyName());
        assertEquals(listing.getMake().getName(), listingResponse.makeName());
        assertEquals(listing.getModel().getName(), listingResponse.modelName());
        assertNull(listingResponse.features());
        assertEquals(listing.getCreatedAt(), listingResponse.createdAt());
        assertEquals(listing.getExpiresAt(), listingResponse.expiresAt());
        assertEquals(listing.getRevoked(), listingResponse.revoked());
        assertEquals(listing.getPrice(), listingResponse.price());
        assertEquals(listing.getProductionYear(), listingResponse.productionYear());
        assertEquals(listing.getMileage(), listingResponse.mileage());
        assertEquals(listing.getFuel(), listingResponse.fuel());
        assertEquals(listing.getCarUsage(), listingResponse.carUsage());
        assertEquals(listing.getCarOperationalStatus(), listingResponse.carOperationalStatus());
        assertEquals(listing.getCarType(), listingResponse.carType());
        assertEquals(listing.getDescription(), listingResponse.description());
    }

    @Test
    public void toListing_ShouldCorrectlyConvert_ListingRequestDTOToListing() {
        // given
        ListingRequest listingRequest = new ListingRequest(
            "1",
                1L,
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

        Mockito.when(makeRepository.findById(1L)).thenReturn(Optional.of(new Make(1L, "Toyota")));
        Mockito.when(modelRepository.findById(1L)).thenReturn(Optional.of(new Model(1L, "Corolla")));
        Mockito.when(companyRepository.findById(1L)).thenReturn(Optional.of(
                new Company(
                        1L,
                        "1",
                        "company1",
                        "address1",
                        "phone1",
                        "email1"
                )
        ));
        List<Long> featureIds = new ArrayList<>(Arrays.asList(1L, 2L));
        List<Feature> featureList = new ArrayList<>(Arrays.asList(
                new Feature(1L, "feature1"),
                new Feature(2L, "feature2")
        ));
        Mockito.when(featureRepository.findAllById(featureIds)).thenReturn(featureList);

        // when

        Listing listing = listingMapper.toListing(listingRequest);

        // then

        assertNotNull(listing);
        assertNull(listing.getId());
        assertEquals(listingRequest.ownerId(), listing.getOwnerId());
        assertEquals("company1", listing.getSellingCompany().getName());
        assertEquals("Toyota", listing.getMake().getName());
        assertEquals("Corolla", listing.getModel().getName());
        assertEquals("feature1", listing.getFeatures().getFirst().getName());
        assertEquals(LocalDate.now(), listing.getCreatedAt());
        assertEquals(listingRequest.expiresAt(), listing.getExpiresAt());
        assertEquals(listingRequest.revoked(), listing.getRevoked());
        assertEquals(listingRequest.price(), listing.getPrice());
        assertEquals(listingRequest.productionYear(), listing.getProductionYear());
        assertEquals(listingRequest.mileage(), listing.getMileage());
        assertEquals(listingRequest.fuel(), listing.getFuel());
        assertEquals(listingRequest.carUsage(), listing.getCarUsage());
        assertEquals(listingRequest.carOperationalStatus(), listing.getCarOperationalStatus());
        assertEquals(listingRequest.carType(), listing.getCarType());
        assertEquals(listingRequest.description(), listing.getDescription());
    }

    @Test
    public void toListing_ShouldCorrectlyConvert_ListingRequestDTOWithNulls_ToListing() {
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

        Mockito.when(makeRepository.findById(1L)).thenReturn(Optional.of(new Make(1L, "Toyota")));
        Mockito.when(modelRepository.findById(1L)).thenReturn(Optional.of(new Model(1L, "Corolla")));
        List<Long> featureIds = new ArrayList<>(Arrays.asList(1L, 2L));
        List<Feature> featureList = new ArrayList<>(Arrays.asList(
                new Feature(1L, "feature1"),
                new Feature(2L, "feature2")
        ));
        Mockito.when(featureRepository.findAllById(featureIds)).thenReturn(featureList);

        // when

        Listing listing = listingMapper.toListing(listingRequest);

        // then

        assertNotNull(listing);
        assertNull(listing.getId());
        assertEquals(listingRequest.ownerId(), listing.getOwnerId());
        assertNull(listing.getSellingCompany());
        assertEquals("Toyota", listing.getMake().getName());
        assertEquals("Corolla", listing.getModel().getName());
        assertEquals("feature1", listing.getFeatures().getFirst().getName());
        assertEquals(LocalDate.now(), listing.getCreatedAt());
        assertEquals(listingRequest.expiresAt(), listing.getExpiresAt());
        assertEquals(listingRequest.revoked(), listing.getRevoked());
        assertEquals(listingRequest.price(), listing.getPrice());
        assertEquals(listingRequest.productionYear(), listing.getProductionYear());
        assertEquals(listingRequest.mileage(), listing.getMileage());
        assertEquals(listingRequest.fuel(), listing.getFuel());
        assertEquals(listingRequest.carUsage(), listing.getCarUsage());
        assertEquals(listingRequest.carOperationalStatus(), listing.getCarOperationalStatus());
        assertEquals(listingRequest.carType(), listing.getCarType());
        assertEquals(listingRequest.description(), listing.getDescription());
    }

    @Test
    public void toListing_ShouldThrowException_WhenWrongIdIsGiven() {
        // given

        Long makeId = 1L;
        Long modelId = 1L;
        Long companyId = 1L;

        ListingRequest listingRequest = new ListingRequest(
                "1",
                companyId,
                makeId,
                modelId,
                null,
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
        
        Mockito.when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then

        assertThrows(IllegalArgumentException.class, () -> {
            listingMapper.toListing(listingRequest);
        });
    }

}
