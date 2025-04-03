package com.msn.msncars.listing;

import org.springframework.stereotype.Service;

@Service
public class ListingMapper {
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
}
