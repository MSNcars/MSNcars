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
    public List<ListingResponse> getAllListings() {
        return listingService.getAllListings();
    }

    @GetMapping(path="{listing-id}")
    public ListingResponse getListingById(@PathVariable("listing-id") Long listingId) {
        return listingService.getListingById(listingId);
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
        ListingResponse listingResponse = listingService.extendExpirationDate(listingId, newExpirationDate);
        return ResponseEntity.ok(listingResponse);
    }

    @DeleteMapping("/{listing-id}")
    public ResponseEntity<Void> deleteListing(@PathVariable("listing-id") Long listingId) {
        listingService.deleteListing(listingId);
        return ResponseEntity.noContent().build();
    }
}
