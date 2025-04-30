package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;

import java.util.List;

public interface ListingService {
    List<ListingResponse> getAllListings();
    ListingResponse getListingById(Long listingId);
    List<ListingResponse> getAllListingFromUser(String userId);
    Long createListing(ListingRequest listingRequest, String userId);
    ListingResponse updateListing(Long listingId, ListingRequest listingRequest, String userId);
    ListingResponse extendExpirationDate(Long listingId, ValidityPeriod validityPeriod, String userId);
    ListingResponse setListingRevokedStatus(Long listingId, boolean isRevoked, String userId);
    void deleteListing(Long listingId, String userId);
}
