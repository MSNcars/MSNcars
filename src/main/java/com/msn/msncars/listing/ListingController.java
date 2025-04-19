package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping(path="/{listing-id}")
    public ListingResponse getListingById(@PathVariable("listing-id") Long listingId) {
        return listingService.getListingById(listingId);
    }

    /*
    @GetMapping(path="/me")
    public List<ListingResponse> getListingsOfRequestingUser(@AuthenticationPrincipal Jwt principal) {
        return listingService.getAllListingFromUser(principal.getSubject());
    }
    */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createListing(@RequestBody @Valid ListingRequest listingRequest, HttpServletResponse response) {
        Long id = listingService.createListing(listingRequest);

        response.setHeader("Location", "/listings/" + id);

        return id;
    }

    @PutMapping("/{listing-id}")
    public ListingResponse updateListing(
            @PathVariable("listing-id") Long listingId,
            @RequestBody @Valid ListingRequest listingRequest) {
        return listingService.updateListing(listingId, listingRequest);
    }

    @PatchMapping("/{listing-id}/extend")
    public ListingResponse extendExpirationDate(
            @PathVariable("listing-id") Long listingId,
            @RequestBody LocalDate newExpirationDate
    ) {
        return listingService.extendExpirationDate(listingId, newExpirationDate);
    }

    @DeleteMapping("/{listing-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@PathVariable("listing-id") Long listingId) {
        listingService.deleteListing(listingId);
    }
}
