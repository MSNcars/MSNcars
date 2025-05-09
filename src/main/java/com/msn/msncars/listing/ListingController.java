package com.msn.msncars.listing;

import com.msn.msncars.car.Fuel;
import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingServiceImpl listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingResponse> getAllListings(
            @RequestParam(required = false) String makeName,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Fuel fuel,
            @RequestParam(required = false) SortAttribute sortAttribute,
            @RequestParam(required = false) SortOrder sortOrder
    ) {
        return listingService.getAllListings(makeName, modelName, fuel, sortAttribute, sortOrder);
    }

    @GetMapping("/{listing-id}")
    public ListingResponse getListingById(@PathVariable("listing-id") Long listingId) {
        return listingService.getListingById(listingId);
    }

    @GetMapping("/me")
    public List<ListingResponse> getListingsOfRequestingUser(@AuthenticationPrincipal Jwt principal) {
        return listingService.getAllListingFromUser(principal.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createListing(@RequestBody @Valid ListingRequest listingRequest, @AuthenticationPrincipal Jwt principal,
                              HttpServletResponse response) {
        Long id = listingService.createListing(listingRequest, principal.getSubject());

        response.setHeader("Location", "/listings/" + id);

        return id;
    }

    @PutMapping("/{listing-id}")
    public ListingResponse updateListing(
            @PathVariable("listing-id") Long listingId,
            @RequestBody @Valid ListingRequest listingRequest,
            @AuthenticationPrincipal Jwt principal
    ) {
        return listingService.updateListing(listingId, listingRequest, principal.getSubject());
    }

    @PatchMapping("/{listing-id}/extend")
    public ListingResponse extendExpirationDate(
            @PathVariable("listing-id") Long listingId,
            @RequestBody ValidityPeriod validityPeriod,
            @AuthenticationPrincipal Jwt principal
    ) {
        return listingService.extendExpirationDate(listingId, validityPeriod, principal.getSubject());
    }

    @PatchMapping("/{listing-id}/set-revoked/{is-revoked}")
    public ListingResponse setListingRevokedStatus(
            @PathVariable("listing-id") Long listingId,
            @PathVariable("is-revoked") boolean isRevoked,
            @AuthenticationPrincipal Jwt principal) {
        return listingService.setListingRevokedStatus(listingId, isRevoked, principal.getSubject());
    }

    @DeleteMapping("/{listing-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@PathVariable("listing-id") Long listingId, @AuthenticationPrincipal Jwt principal) {
        listingService.deleteListing(listingId, principal.getSubject());
    }
}
