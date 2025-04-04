package com.msn.msncars.listing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public ResponseEntity<List<ListingResponse>> getAllListings() {
        List<ListingResponse> listings = listingService.getAllListings();

        return ResponseEntity.ok(listings);
    }

    @GetMapping(path="{listing-id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable("listing-id") Long listingId) {
        ListingResponse listing = listingService.getListingById(listingId);

        return ResponseEntity.ok(listing);
    }
}
