package com.msn.msncars.listing;

import com.msn.msncars.car.FeatureRepository;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.car.exception.ModelNotFoundException;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.company.exception.CompanyNotFoundException;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingNotFoundException;
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

    /*
    public List<ListingResponse> getAllListingFromUser(String userId) {
        List<Listing> listings = listingRepository.findAllByOwnerId(userId);
        List<ListingResponse> listingResponses = new ArrayList<>();

        for (Listing listing : listings) {
            listingResponses.add(listingMapper.fromListing(listing));
        }

        return listingResponses;
    }
     */

    public Long createListing(ListingRequest listingRequest) {
        Listing listing = listingMapper.fromDTO(listingRequest);

        // Set correct relation with other entities based on provided IDs
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

        // Set creation and expiration time
        listing.setCreatedAt(ZonedDateTime.now());
        listing.setExpiresAt(listing.getCreatedAt().plusDays(listingRequest.validityPeriod().numberOfDays));

        return listingRepository.save(listing).getId();
    }

    public ListingResponse updateListing(Long listingId, ListingRequest listingRequest) {
        listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        Listing updatedListing = listingMapper.fromDTO(listingRequest);
        updatedListing.setId(listingId);

        // Set correct relation with other entities based on provided IDs
        updatedListing.setOwnerId(listingRequest.ownerId());
        if (listingRequest.sellingCompanyId() != null){// Setting selling company is optional
            updatedListing.setSellingCompany(
                    companyRepository.findById(listingRequest.sellingCompanyId())
                            .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + listingRequest.sellingCompanyId()))
            );
        }
        updatedListing.setModel(
                modelRepository.findById(listingRequest.modelId())
                        .orElseThrow(() -> new ModelNotFoundException("Model not found with id: " + listingRequest.modelId()))
        );
        updatedListing.setFeatures(
                featureRepository.findAllById(listingRequest.featuresIds())
        );

        Listing savedListing = listingRepository.save(updatedListing);

        return listingMapper.toDTO(savedListing);
    }

    public ListingResponse extendExpirationDate(Long listingId, ValidityPeriod validityPeriod) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        listing.setExpiresAt(listing.getExpiresAt().plusDays(validityPeriod.numberOfDays));
        Listing updatedListing = listingRepository.save(listing);

        return listingMapper.toDTO(updatedListing);
    }

    public void deleteListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        listingRepository.delete(listing);
    }
}
