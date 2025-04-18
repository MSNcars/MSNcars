package com.msn.msncars.listing;

import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.car.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListingMapper {
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final CompanyRepository companyRepository;
    private final FeatureRepository featureRepository;

    public ListingMapper(MakeRepository makeRepository, ModelRepository modelRepository,
                         CompanyRepository companyRepository, FeatureRepository featureRepository) {
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.companyRepository = companyRepository;
        this.featureRepository = featureRepository;
    }

    // in this method we have to consider all Listing attributes that can be null!
    public Listing toListing (ListingRequest listingRequest) {
        return new Listing(
                null,
                listingRequest.ownerId(),
                listingRequest.sellingCompanyId() != null ? getCompanyById(listingRequest.sellingCompanyId()) : null,
                getMakeById(listingRequest.makeId()),
                getModelById(listingRequest.modelId()),
                listingRequest.featuresIds() != null ? getFeaturesByIds(listingRequest.featuresIds()) : null,
                LocalDate.now(),
                listingRequest.expiresAt() != null ? listingRequest.expiresAt() : null,
                listingRequest.revoked() != null ? listingRequest.revoked() : null,
                listingRequest.price() != null ? listingRequest.price() : null,
                listingRequest.productionYear() != null ? listingRequest.productionYear() : null,
                listingRequest.mileage() != null ? listingRequest.mileage() : null,
                listingRequest.fuel() != null ? listingRequest.fuel() : null,
                listingRequest.carUsage() != null ? listingRequest.carUsage() : null,
                listingRequest.carOperationalStatus() != null ? listingRequest.carOperationalStatus() : null,
                listingRequest.carType() != null ? listingRequest.carType() : null,
                listingRequest.description() != null ? listingRequest.description() : null
        );
    }

    // in this method we have to consider all Listing attributes that can be null!
    public ListingResponse fromListing (Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getOwnerId(),
                listing.getSellingCompany() != null ? listing.getSellingCompany().getName() : null,
                listing.getMake().getName(),
                listing.getModel().getName() != null ? listing.getModel().getName() : null,
                listing.getFeatures() != null ? listing.getFeatures() : null,
                listing.getCreatedAt() != null ? listing.getCreatedAt() : null,
                listing.getExpiresAt() != null ? listing.getExpiresAt() : null,
                listing.getRevoked() != null ? listing.getRevoked() : null,
                listing.getPrice() != null ? listing.getPrice() : null,
                listing.getProductionYear() != null ? listing.getProductionYear() : null,
                listing.getMileage() != null ? listing.getMileage() : null,
                listing.getFuel() != null ? listing.getFuel() : null,
                listing.getCarUsage() != null ? listing.getCarUsage() : null,
                listing.getCarOperationalStatus() != null ? listing.getCarOperationalStatus() : null,
                listing.getCarType() != null ? listing.getCarType() : null,
                listing.getDescription() != null ? listing.getDescription() : null
        );
    }

    private Make getMakeById(Long id) {
        return makeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Make not found with id: " + id));
    }

    private Model getModelById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with id: " + id));
    }

    private Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }

    private List<Feature> getFeaturesByIds(List<Long> featuresIds) {
        return featureRepository.findAllById(featuresIds);
    }
}
