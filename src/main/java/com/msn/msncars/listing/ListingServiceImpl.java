package com.msn.msncars.listing;

import com.msn.msncars.car.FeatureRepository;
import com.msn.msncars.car.Fuel;
import com.msn.msncars.car.exception.ModelNotFoundException;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.company.exception.CompanyNotFoundException;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingExpiredException;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import com.msn.msncars.listing.exception.ListingRevokedException;
import jakarta.ws.rs.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ListingServiceImpl implements ListingService{
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    private final ModelRepository modelRepository;
    private final CompanyRepository companyRepository;
    private final FeatureRepository featureRepository;

    private final Clock clock;

    private final Logger logger = LoggerFactory.getLogger(ListingServiceImpl.class);

    public ListingServiceImpl(ListingRepository listingRepository, ListingMapper listingMapper, ModelRepository modelRepository, CompanyRepository companyRepository, FeatureRepository featureRepository, Clock clock) {
        this.listingRepository = listingRepository;
        this.listingMapper = listingMapper;
        this.modelRepository = modelRepository;
        this.companyRepository = companyRepository;
        this.featureRepository = featureRepository;
        this.clock = clock;
    }

    public List<ListingResponse> getAllListings(String makeName, String modelName, Fuel fuel, SortAttribute sortAttribute, SortOrder sortOrder) {
        logger.debug("Entering getAllListings method.");

        List<Listing> listings = listingRepository.findAll();
        List<ListingResponse> listingResponses = new ArrayList<>();

        Stream<Listing> stream = listings.stream();

        logger.debug("Listings fetched from database, starting filtration by search criteria.");

        if (makeName != null) {
            stream = stream.filter(l -> l.getModel().getMake().getName().equalsIgnoreCase(makeName));
        }

        if (modelName != null) {
            stream = stream.filter(l -> l.getModel().getName().equalsIgnoreCase(modelName));
        }

        if (fuel != null) {
            stream = stream.filter(l -> l.getFuel() == fuel);
        }

        if (sortAttribute != null && sortOrder != null) {
            stream = stream.sorted(sortAttribute.getComparator(sortOrder));
        }

        listings = stream.toList();

        logger.debug("Listings filtered by search criteria retrieved, starting mapping to DTO.");

        for (Listing listing : listings) {
            listingResponses.add(listingMapper.toDTO(listing, clock.getZone()));
        }

        logger.debug("Listings mapped to DTOs.");

        return listingResponses;
    }

    public ListingResponse getListingById(Long listingId) {
        logger.debug("Entering getListingById method for listing with id {}",  listingId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        logger.debug("Listing with id {} found, starting mapping operation.", listingId);

        ListingResponse listingResponse = listingMapper.toDTO(listing, clock.getZone());

        logger.debug("Listing with id {} successfully mapped to ListingResponse.", listingId);

        return listingResponse;
    }

    public List<ListingResponse> getAllListingFromUser(String userId) {
        logger.debug("Entering getAllListingFromUser method for user with id {}", userId);

        List<Listing> listings = listingRepository.findAllByOwnerId(userId);

        logger.debug("All listings ({}) found for user with id {}", listings.size(), userId);

        List<ListingResponse> listingResponses = new ArrayList<>();
        for (Listing listing : listings) {
            if (listing.getOwnerType() != OwnerType.COMPANY)
                listingResponses.add(listingMapper.toDTO(listing, clock.getZone()));
        }

        logger.debug("Listings successfully mapped to ListingResponses.");

        return listingResponses;
    }

    public Long createListing(ListingRequest listingRequest, String userId) {
        logger.debug("Entering createListing method for user with id {}", userId);
        Listing listing = listingMapper.fromDTO(listingRequest);

        if (listing.getOwnerType() == OwnerType.USER) {// -> Override ownerId with userId from JWT
            listing.setOwnerId(userId);
            logger.debug("OwnerType set to USER, will override listing ownerId using userId from JWT");
        }

        validateListingOwnership(listing, userId);

        logListingOwnershipValidatedSuccessfully();

        fetchEntities(listing, listingRequest);

        logger.debug("Listing model and features fetched successfully.");

        listing.setCreatedAt(clock.instant());
        listing.setExpiresAt(listing.getCreatedAt().plus(listingRequest.validityPeriod().getNumberOfDays(), ChronoUnit.DAYS));

        logger.debug("Listing creation and expiration dates set successfully.");

        return listingRepository.save(listing).getId();
    }

    public ListingResponse updateListing(Long listingId, ListingRequest listingRequest, String userId) {
        logger.debug("Entering updateListing method for listing with id {} and user with id {}", listingId, userId);
        Listing oldListing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        logger.debug("Listing with id {} successfully retrieved from database.", listingId);

        validateListingOwnership(oldListing, userId);

        logListingOwnershipValidatedSuccessfully();

        validateListingActive(oldListing);

        logger.debug("Listing status successfully validated.");

        Listing updatedListing = listingMapper.fromDTO(listingRequest);
        updatedListing.setId(listingId);
        updatedListing.setCreatedAt(oldListing.getCreatedAt());
        updatedListing.setExpiresAt(oldListing.getExpiresAt());

        logger.debug("Updated listing created successfully.");

        fetchEntities(updatedListing, listingRequest);

        logger.debug("Updated listing model and features fetched successfully.");

        ListingResponse listingResponse = listingMapper.toDTO(listingRepository.save(updatedListing), clock.getZone());

        logger.debug("Listing updated and mapped successfully.");

        return listingResponse;
    }

    public ListingResponse extendExpirationDate(Long listingId, ValidityPeriod validityPeriod, String userId) {
        logger.debug("Entering extendExpirationDate method for listing with id {} and user with id {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        logListingSuccessfullyFetchedFromDb(listingId);

        if(listing.getRevoked()){
            throw new ListingRevokedException("Cannot extend revoked listing.");
        }

        logger.debug("Listing revoked field validated successfully.");

        validateListingOwnership(listing, userId);

        logListingOwnershipValidatedSuccessfully();

        Instant startingTime = listing.getExpiresAt().isAfter(clock.instant()) ? listing.getExpiresAt() : clock.instant();
        listing.setExpiresAt(startingTime.plus(validityPeriod.getNumberOfDays(), ChronoUnit.DAYS));

        logger.debug("Listing expiration date extended successfully.");

        Listing updatedListing = listingRepository.save(listing);
        ListingResponse listingResponse = listingMapper.toDTO(updatedListing, clock.getZone());

        logger.debug("Listing updated and mapped to DTO successfully.");

        return listingResponse;
    }

    public ListingResponse setListingRevokedStatus(Long listingId, boolean isRevoked, String userId) {
        logger.debug("Entering setListingRevokedStatus method for listing with id {} and user with id {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        logListingSuccessfullyFetchedFromDb(listingId);

        validateListingOwnership(listing, userId);

        logListingOwnershipValidatedSuccessfully();

        listing.setRevoked(isRevoked);

        logger.debug("Listing revoked field set to {}", isRevoked);

        Listing updatedListing = listingRepository.save(listing);
        ListingResponse listingResponse = listingMapper.toDTO(updatedListing, clock.getZone());

        logger.debug("Listing updated and mapped to DTO successfully.");

        return listingResponse;
    }

    public void deleteListing(Long listingId, String userId) {
        logger.debug("Entering deleteListing method for listing with id {} and user with id {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        logListingSuccessfullyFetchedFromDb(listingId);

        validateListingOwnership(listing, userId);

        logListingOwnershipValidatedSuccessfully();

        listingRepository.delete(listing);

        logger.debug("Listing deleted successfully.");
    }

    /*
        Validates if user has access to modifying the listing.
     */
    public void validateListingOwnership(Listing listing, String userId){
        switch (listing.getOwnerType()){
            case USER -> {
                if (!listing.getOwnerId().equals(userId)){
                    throw new ForbiddenException("You don't have permission to this listing.");
                }
            }
            case COMPANY -> {
                Company company = companyRepository.findById(Long.valueOf(listing.getOwnerId()))
                        .orElseThrow(() -> new CompanyNotFoundException("Company not found with id " + listing.getOwnerId()));
                if (!company.hasMember(userId)){
                    throw new ForbiddenException("You don't have permission to this listing.");
                }
            }
        }
    }

    public void validateListingActive(Listing listing){
        if(listing.getRevoked()){
            throw new ListingRevokedException("Cannot modify revoked listing.");
        }
        if (listing.getExpiresAt().isBefore(clock.instant())){
            throw new ListingExpiredException("You can't edit listing that already expired.");
        }
    }

    /*
        Fetch other entities based on provided IDs.
    */
    private void fetchEntities(Listing listing, ListingRequest listingRequest){
        listing.setModel(
                modelRepository.findById(listingRequest.modelId())
                        .orElseThrow(() -> new ModelNotFoundException("Model not found with id: " + listingRequest.modelId()))
        );

        listing.setFeatures(
                featureRepository.findAllById(listingRequest.featuresIds())
        );
    }

    private void logListingOwnershipValidatedSuccessfully() {
        logger.debug("Listing ownership validated successfully.");
    }

    private void logListingSuccessfullyFetchedFromDb(Long listingId) {
        logger.debug("Listing with id {} successfully fetched from database.", listingId);
    }
}
