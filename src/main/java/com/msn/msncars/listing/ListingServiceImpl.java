package com.msn.msncars.listing;

import com.msn.msncars.car.FeatureRepository;
import com.msn.msncars.car.Fuel;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.car.exception.ModelNotFoundException;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.company.exception.CompanyNotFoundException;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import com.msn.msncars.listing.exception.ListingRevokedException;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ListingServiceImpl implements ListingService{
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    private final ModelRepository modelRepository;
    private final CompanyRepository companyRepository;
    private final FeatureRepository featureRepository;

    public ListingServiceImpl(ListingRepository listingRepository, ListingMapper listingMapper, ModelRepository modelRepository, CompanyRepository companyRepository, FeatureRepository featureRepository) {
        this.listingRepository = listingRepository;
        this.listingMapper = listingMapper;
        this.modelRepository = modelRepository;
        this.companyRepository = companyRepository;
        this.featureRepository = featureRepository;
    }

    public List<ListingResponse> getAllListings(String makeName, String modelName, Fuel fuel, SortAttribute sortAttribute, SortOrder sortOrder) {
        List<Listing> listings = listingRepository.findAll();
        List<ListingResponse> listingResponses = new ArrayList<>();

        Stream<Listing> stream = listings.stream();

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

        for (Listing listing : listings) {
            listingResponses.add(listingMapper.toDTO(listing));
        }

        return listingResponses;
    }

    public ListingResponse getListingById(Long listingId) {
        Optional<Listing> listing = listingRepository.findById(listingId);

        return listingMapper.toDTO(
                listing.orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId))
        );
    }

    public List<ListingResponse> getAllListingFromUser(String userId) {
        List<Listing> listings = listingRepository.findAllByOwnerId(userId);

        List<ListingResponse> listingResponses = new ArrayList<>();
        for (Listing listing : listings) {
            if (listing.getOwnerType() != OwnerType.COMPANY)
                listingResponses.add(listingMapper.toDTO(listing));
        }

        return listingResponses;
    }

    public Long createListing(ListingRequest listingRequest, String userId) {
        Listing listing = listingMapper.fromDTO(listingRequest);

        validateListingOwnership(listing, userId);

        listing.setOwnerId(userId);

        fetchEntities(listing, listingRequest);

        // Set creation and expiration time
        listing.setCreatedAt(ZonedDateTime.now());
        listing.setExpiresAt(listing.getCreatedAt().plusDays(listingRequest.validityPeriod().numberOfDays));

        return listingRepository.save(listing).getId();
    }

    public ListingResponse updateListing(Long listingId, ListingRequest listingRequest, String userId) {
        Listing oldListing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        if(oldListing.getRevoked()){
            throw new ListingRevokedException("Cannot update revoked listing.");
        }

        validateListingOwnership(oldListing, userId);

        Listing updatedListing = listingMapper.fromDTO(listingRequest);
        updatedListing.setId(listingId);

        fetchEntities(updatedListing, listingRequest);

        return listingMapper.toDTO(listingRepository.save(updatedListing));
    }

    public ListingResponse extendExpirationDate(Long listingId, ValidityPeriod validityPeriod, String userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        if(listing.getRevoked()){
            throw new ListingRevokedException("Cannot extend revoked listing.");
        }

        validateListingOwnership(listing, userId);

        ZonedDateTime startingTime = listing.getExpiresAt().isAfter(ZonedDateTime.now()) ? listing.getExpiresAt() : ZonedDateTime.now();
        listing.setExpiresAt(startingTime.plusDays(validityPeriod.numberOfDays));

        Listing updatedListing = listingRepository.save(listing);

        return listingMapper.toDTO(updatedListing);
    }

    public ListingResponse setListingRevokedStatus(Long listingId, boolean isRevoked, String userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        validateListingOwnership(listing, userId);

        listing.setRevoked(isRevoked);

        Listing updatedListing = listingRepository.save(listing);

        return listingMapper.toDTO(updatedListing);
    }

    public void deleteListing(Long listingId, String userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        validateListingOwnership(listing, userId);

        listingRepository.delete(listing);
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
                if (!company.getMembers().contains(userId)){
                    throw new ForbiddenException("You don't have permission to this listing.");
                }
            }
        }
    }

    /*
        Fetch other entities based on provided IDs.
    */
    private void fetchEntities(Listing listing, ListingRequest listingRequest){
        listing.setOwnerId(listingRequest.ownerId());

        listing.setModel(
                modelRepository.findById(listingRequest.modelId())
                        .orElseThrow(() -> new ModelNotFoundException("Model not found with id: " + listingRequest.modelId()))
        );

        listing.setFeatures(
                featureRepository.findAllById(listingRequest.featuresIds())
        );
    }
}
