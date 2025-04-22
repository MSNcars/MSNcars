package com.msn.msncars.listing;

import com.msn.msncars.car.FeatureRepository;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.car.exception.ModelNotFoundException;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.company.exception.CompanyNotFoundException;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingExpiredException;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import com.msn.msncars.listing.exception.ListingRevokedException;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<ListingResponse> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        List<ListingResponse> listingResponses = new ArrayList<>();

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
            listingResponses.add(listingMapper.toDTO(listing));
        }

        return listingResponses;
    }

    public Long createListing(ListingRequest listingRequest, String userId) {
        Listing listing = listingMapper.fromDTO(listingRequest);
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

        validateListingOwnership(oldListing, userId);
        validateListingActive(oldListing);

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

    public void deleteListing(Long listingId, String userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        validateListingOwnership(listing, userId);

        listingRepository.delete(listing);
    }

    public void validateListingOwnership(Listing listing, String userId){
        if(
            !listing.getOwnerId().equals(userId) &&
            (listing.getSellingCompany() != null && !listing.getSellingCompany().getUsersId().contains(userId))
        ){
            throw new ForbiddenException("You don't have permission to edit this listing.");
        }
    }

    public void validateListingActive(Listing listing){
        if(listing.getRevoked()){
            throw new ListingRevokedException("Cannot extend revoked listing.");
        }
        if (listing.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new ListingExpiredException("You can't edit listing that already expired.");
        }
    }

    /*
        Fetch other entities based on provided IDs.
    */
    private void fetchEntities(Listing listing, ListingRequest listingRequest){
        listing.setOwnerId(listingRequest.ownerId());

        if (listingRequest.sellingCompanyId() != null){// Setting selling company is optional
            listing.setSellingCompany(
                    companyRepository.findById(listingRequest.sellingCompanyId())
                            .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + listingRequest.sellingCompanyId()))
            );
        }

        listing.setModel(
                modelRepository.findById(listingRequest.modelId())
                        .orElseThrow(() -> new ModelNotFoundException("Model not found with id: " + listingRequest.modelId()))
        );

        listing.setFeatures(
                featureRepository.findAllById(listingRequest.featuresIds())
        );
    }
}
