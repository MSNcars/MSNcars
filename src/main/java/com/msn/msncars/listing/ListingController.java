package com.msn.msncars.listing;

import com.msn.msncars.car.Fuel;
import com.msn.msncars.listing.dto.ListingRequest;
import com.msn.msncars.listing.dto.ListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;

    private final Logger logger = LoggerFactory.getLogger(ListingController.class);

    public ListingController(ListingServiceImpl listingService) {
        this.listingService = listingService;
    }

    @Operation(summary="Get all existing listings with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings fetched successfully")
    })
    @GetMapping
    public List<ListingResponse> getAllListings(
            @RequestParam(required = false) String makeName,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Fuel fuel,
            @RequestParam(required = false) SortAttribute sortAttribute,
            @RequestParam(required = false) SortOrder sortOrder
    ) {
        logger.info("Received request to get all listings.");

        List<ListingResponse> allListingsResponses = listingService.getAllListings(makeName, modelName, fuel, sortAttribute, sortOrder);

        logger.info("All listings successfully retrieved.");

        return allListingsResponses;
    }

    @Operation(summary = "Get listing with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Listing not found", content = @Content)
    })
    @GetMapping("/{listing-id}")
    public ListingResponse getListingById(@PathVariable("listing-id") Long listingId) {
        logger.info("Received request to get listing by id {}.", listingId);

        ListingResponse listingResponse = listingService.getListingById(listingId);

        logger.info("Listing with id {} successfully retrieved.", listingId);

        return listingResponse;
    }

    @Operation(summary = "Get all listings of signed in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings fetched successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public List<ListingResponse> getListingsOfRequestingUser(@AuthenticationPrincipal Jwt principal) {
        String userId = principal.getSubject();
        logger.info("Received request to get listings of requesting user (id: {}).", userId);

        List<ListingResponse> allListingsResponses = listingService.getAllListingFromUser(userId);

        logger.info("All listings successfully retrieved for user with id {}.", userId);

        return allListingsResponses;
    }

    @Operation(summary = "Create a new listing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Listing created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Model or company was not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createListing(@RequestBody @Valid ListingRequest listingRequest, @AuthenticationPrincipal Jwt principal,
                              HttpServletResponse response) {
        String userId = principal.getSubject();
        logger.info("Received request to create listing for user with id: {}.", userId);

        Long id = listingService.createListing(listingRequest, userId);

        logger.info("Listing with id {} successfully created.", id);

        response.setHeader("Location", "/listings/" + id);

        return id;
    }

    @Operation(summary = "Edit listing with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing edited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "You are not owner of the listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or model was not found", content = @Content),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{listing-id}")
    public ListingResponse updateListing(
            @PathVariable("listing-id") Long listingId,
            @RequestBody @Valid ListingRequest listingRequest,
            @AuthenticationPrincipal Jwt principal
    ) {
        logger.info("Received request to update listing with id {}.", listingId);

        ListingResponse listingResponse = listingService.updateListing(listingId, listingRequest, principal.getSubject());

        logger.info("Listing with id {} successfully updated.", listingId);

        return listingResponse;
    }

    @Operation(summary = "Extend expiration date of a listing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing extended successfully"),
            @ApiResponse(responseCode = "400", description = "Listing is revoked", content = @Content),
            @ApiResponse(responseCode = "403", description = "You are not owner of the listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or company was not found", content = @Content),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{listing-id}/extend")
    public ListingResponse extendExpirationDate(
            @PathVariable("listing-id") Long listingId,
            @RequestBody ValidityPeriod validityPeriod,
            @AuthenticationPrincipal Jwt principal
    ) {
        logger.info("Received request to extend listing with id {}.", listingId);

        ListingResponse listingResponse = listingService.extendExpirationDate(listingId, validityPeriod, principal.getSubject());

        logger.info("Listing with id {} successfully extended.", listingId);

        return listingResponse;
    }

    @Operation(summary = "Set revoked status of a listing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing revoked status set successfully"),
            @ApiResponse(responseCode = "403", description = "You are not owner of the listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or company was not found", content = @Content),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{listing-id}/set-revoked/{is-revoked}")
    public ListingResponse setListingRevokedStatus(
            @PathVariable("listing-id") Long listingId,
            @PathVariable("is-revoked") boolean isRevoked,
            @AuthenticationPrincipal Jwt principal) {
        logger.info("Received request to set listing revoked for listing with id {}.", listingId);

        ListingResponse listingResponse = listingService.setListingRevokedStatus(listingId, isRevoked, principal.getSubject());

        logger.info("Revoke field of listing with id {} set to value {}.", listingId, isRevoked);

        return listingResponse;
    }

    @Operation(summary = "Delete listing with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Listing deleted successfully"),
            @ApiResponse(responseCode = "403", description = "You are not owner of the listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or company was not found", content = @Content),
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{listing-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@PathVariable("listing-id") Long listingId, @AuthenticationPrincipal Jwt principal) {
        logger.info("Received request to delete listing with id {}.", listingId);

        listingService.deleteListing(listingId, principal.getSubject());

        logger.info("Listing with id {} successfully deleted.", listingId);
    }
}
