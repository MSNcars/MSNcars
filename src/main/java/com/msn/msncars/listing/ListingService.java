package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;

public interface ListingService {
    List<ListingResponse> getAllListings();
    ListingResponse getListingById(Long listingId);
    Long createListing(ListingRequest listingRequest);
    ListingResponse updateListing(Long listingId, ListingRequest listingRequest);
    ListingResponse extendExpirationDate(Long listingId, ValidityPeriod validityPeriod);
    void deleteListing(Long listingId);
}
