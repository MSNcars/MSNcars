package com.msn.msncars.listing;

import com.msn.msncars.account.Company;
import com.msn.msncars.account.CompanyRepository;
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

    public Listing toListing (ListingRequest listingRequest) {
        return new Listing(
                listingRequest.id(),
                listingRequest.ownerId(),
                getCompanyById(listingRequest.sellingCompanyId()),
                getMakeById(listingRequest.makeId()),
                getModelById(listingRequest.modelId()),
                getFeaturesByIds(listingRequest.featuresIds()),
                LocalDate.now(),
                listingRequest.expiresAt(),
                listingRequest.revoked(),
                listingRequest.price(),
                listingRequest.productionYear(),
                listingRequest.mileage(),
                listingRequest.fuel(),
                listingRequest.carUsage(),
                listingRequest.carOperationalStatus(),
                listingRequest.carType(),
                listingRequest.description()
        );
    }
    public ListingResponse fromListing (Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getOwnerId(),
                listing.getSellingCompany().getId(),
                listing.getMake().getId(),
                listing.getModel().getId(),
                listing.getFeatures(),
                listing.getCreatedAt(),
                listing.getExpiresAt(),
                listing.getRevoked(),
                listing.getPrice(),
                listing.getProductionYear(),
                listing.getMileage(),
                listing.getFuel(),
                listing.getCarUsage(),
                listing.getCarOperationalStatus(),
                listing.getCarType(),
                listing.getDescription()
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
