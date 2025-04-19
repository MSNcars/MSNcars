package com.msn.msncars.listing;

import com.msn.msncars.car.*;
import com.msn.msncars.company.Company;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingExpirationDateException;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingService listingService;

    @Captor
    private ArgumentCaptor<Listing> listingArgumentCaptor;

    @Test
    public void getAllListings_ShouldReturnAllOfThem_WhenListingsExist() {
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
                null,
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

        // when

        List<ListingResponse> listingResponses = listingService.getAllListings();

        // then

        assertEquals(3, listingResponses.size());
        assertEquals(listing1.getId(), listingResponses.getFirst().id());
        assertEquals(listing1.getModel().getName(), listingResponses.getFirst().modelName());
        assertEquals(listing2.getMake().getName(), listingResponses.get(1).makeName());
        assertEquals(listing2.getSellingCompany().getName(), listingResponses.get(1).sellingCompanyName());
        assertNull(listingResponses.get(2).sellingCompanyName());
        assertEquals(listing3.getMileage(), listingResponses.get(2).mileage());
    }

    @Test
    public void getListingById_ShouldReturnListing_WhenRequestedListingExists() {
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

        Mockito.when(listingRepository.findById(1L)).thenReturn(Optional.of(listing1));

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

        Mockito.when(listingMapper.fromListing(listing1)).thenReturn(listingResponse1);

        // when

        ListingResponse listingResponse = listingService.getListingById(1L);

        // then

        assertNotNull(listingResponse);
        assertEquals(listing1.getId(), listingResponse.id());
        assertEquals(listing1.getSellingCompany().getName(), listingResponse.sellingCompanyName());
        assertEquals(listing1.getFeatures().getFirst().getName(), listingResponse.features().getFirst().getName());
    }

    @Test
    public void getListingById_ShouldThrowException_WhenRequestedListingDoesNotExist() {
        // given
        Long listingId = 1L;

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        // when & then

        assertThrows(ListingNotFoundException.class, () -> listingService.getListingById(listingId));
    }

    @Test
    public void createListing_ShouldReturnIdOfCreatedListing_AndCreateListing() {
        // given

        Long listingId = 1L;

        ListingRequest listingRequest = new ListingRequest(
                "1",
                null,
                1L,
                1L,
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

        Listing listingFromRequest = new Listing(
                listingId,
                "1",
                null,
                new Make(1L, "Toyota"),
                new Model(1L, "Yaris"),
                null,
                LocalDate.now(),
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

        Mockito.when(listingMapper.toListing(listingRequest)).thenReturn(listingFromRequest);
        Mockito.when(listingRepository.save(listingFromRequest)).thenReturn(listingFromRequest);

        // when

        Long createdListingId = listingService.createListing(listingRequest);

        // then

        assertEquals(listingId, createdListingId);

        Mockito.verify(listingRepository).save(listingFromRequest);
        Mockito.verify(listingMapper).toListing(listingRequest);

        Mockito.verify(listingRepository).save(listingArgumentCaptor.capture());
        Listing saved = listingArgumentCaptor.getValue();
        assertEquals("1", saved.getOwnerId());
        assertEquals(CarType.COUPE, saved.getCarType());
        assertEquals("desc2", saved.getDescription());
    }

    @Test
    public void UpdateListing_ShouldUpdateListing_AndReturnUpdatedListing_WhenGivenIdIsValid() {
        // given

        Long listingId = 1L;

        ListingRequest listingRequest = new ListingRequest(
                "1",
                null,
                1L,
                1L,
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

        Listing listingFromRequest = new Listing(
                listingId,
                "1",
                null,
                new Make(1L, "Toyota"),
                new Model(1L, "Yaris"),
                null,
                LocalDate.now(),
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

        ListingResponse listingResponse = new ListingResponse(
                1L,
                "1",
                null,
                "Toyota",
                "Yaris",
                null,
                LocalDate.now(),
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

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.of(new Listing()));
        Mockito.when(listingMapper.toListing(listingRequest)).thenReturn(listingFromRequest);
        Mockito.when(listingRepository.save(listingFromRequest)).thenReturn(listingFromRequest);
        Mockito.when(listingMapper.fromListing(listingFromRequest)).thenReturn(listingResponse);

        // when

        ListingResponse response = listingService.updateListing(listingId, listingRequest);

        // then

        assertNotNull(response);
        assertEquals(listingId, response.id());
        assertEquals(listingRequest.mileage(), response.mileage());
        assertEquals(listingRequest.carType(), response.carType());

        Mockito.verify(listingRepository).findById(listingId);
        Mockito.verify(listingMapper).toListing(listingRequest);
        Mockito.verify(listingRepository).save(listingFromRequest);
        Mockito.verify(listingMapper).fromListing(listingFromRequest);

        Mockito.verify(listingRepository).save(listingArgumentCaptor.capture());
        Listing saved = listingArgumentCaptor.getValue();
        assertEquals(listingId, saved.getId());
        assertEquals(listingRequest.price(), saved.getPrice());
    }

    @Test
    public void UpdateListing_ShouldThrowException_WhenGivenIdIsInvalid() {
        // given

        Long listingId = 1L;

        ListingRequest listingRequest = new ListingRequest(
                "1",
                null,
                1L,
                1L,
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

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        // when & then

        assertThrows(ListingNotFoundException.class, () -> listingService.updateListing(listingId, listingRequest));

    }

    @Test
    public void ExtendExpirationDate_ShouldReturnUpdatedListing_WhenIdAndDateAreValid() {
        // given

        Long listingId = 1L;

        LocalDate newExpirationDate = LocalDate.now().plusDays(45);

        Listing listing = new Listing(
                listingId,
                "1",
                null,
                new Make(1L, "Toyota"),
                new Model(1L, "Yaris"),
                null,
                LocalDate.now(),
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

        Listing updatedListing = new Listing(listing);
        updatedListing.setExpiresAt(newExpirationDate);

        ListingResponse listingResponse = new ListingResponse(
                listingId,
                "1",
                null,
                "Toyota",
                "Yaris",
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(45),
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

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        Mockito.when(listingRepository.save(Mockito.any(Listing.class))).thenReturn(updatedListing);
        Mockito.when(listingMapper.fromListing(updatedListing)).thenReturn(listingResponse);

        // when

        ListingResponse response = listingService.extendExpirationDate(listingId, newExpirationDate);

        // then

        assertNotNull(response);
        assertEquals(listingId, response.id());
        assertEquals(newExpirationDate, response.expiresAt());

        Mockito.verify(listingRepository).findById(listingId);
        Mockito.verify(listingRepository).save(Mockito.any(Listing.class));
        Mockito.verify(listingMapper).fromListing(updatedListing);

        Mockito.verify(listingRepository).save(listingArgumentCaptor.capture());
        Listing saved = listingArgumentCaptor.getValue();
        assertEquals(newExpirationDate, saved.getExpiresAt());
        assertEquals(listingId, saved.getId());
    }

    @Test
    public void ExtendExpirationDate_ShouldThrowException_WhenIdIsNotValid() {
        // given

        Long listingId = 1L;

        LocalDate date = LocalDate.now().plusDays(20);

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        // when & then

        assertThrows(ListingNotFoundException.class, () -> listingService.extendExpirationDate(listingId, date));
    }

    @Test
    public void ExtendExpirationDate_ShouldThrowException_WhenDateIsNotValid() {
        // given

        Long listingId = 1L;

        LocalDate date = LocalDate.now().minusDays(20);

        Listing listing = new Listing();
        listing.setId(listingId);

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        // when & then

        assertThrows(ListingExpirationDateException.class, () -> listingService.extendExpirationDate(listingId, date));
    }

    @Test
    public void deleteListing_ShouldDeleteListing_WhenListingExists() {
        // given

        Long listingId = 1L;
        Listing listing = new Listing();
        listing.setId(listingId);

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        // when

        listingService.deleteListing(listingId);

        // then

        Mockito.verify(listingRepository).findById(listingId);
        Mockito.verify(listingRepository).delete(listing);
    }

    @Test
    public void DeleteListing_ShouldThrowException_WhenGivenIdIsNotValid() {
        // given

        Long listingId = 1L;

        Mockito.when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        //when & then

        assertThrows(ListingNotFoundException.class, () -> listingService.deleteListing(listingId));
    }
}
