package com.msn.msncars.listing;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        ListingResponse listing = listingService.getListingResponseById(listingId);

        return ResponseEntity.ok(listing);
    }

    @PostMapping
    public ResponseEntity<Long> createListing(@RequestBody @Valid ListingRequest listingRequest) {
        return ResponseEntity.ok(listingService.createListing(listingRequest));
    }

    @PutMapping("/{listing-id}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable("listing-id") Long listingId,
            @RequestBody @Valid ListingRequest listingRequest) {
        ListingResponse updatedListing = listingService.updateListing(listingId, listingRequest);
        return ResponseEntity.ok(updatedListing);
    }

    @PatchMapping("/{listing-id}/extend")
    public ResponseEntity<ListingResponse> extendExpirationDate(
            @PathVariable("listing-id") Long listingId,
            @RequestBody LocalDate newExpirationDate
    ) {
        ListingResponse listingResponse= listingService.extendExpirationDate(listingId, newExpirationDate);
        return ResponseEntity.ok(listingResponse);
    }
}
