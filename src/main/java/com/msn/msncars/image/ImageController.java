package com.msn.msncars.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ImageController {
    private final ImageService imageService;
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "Attach image to the listing", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image attached successfully"),
            @ApiResponse(responseCode = "404", description = "Listing not found", content = @Content)
    })
    @PostMapping("images")
    @ResponseStatus(HttpStatus.CREATED)
    public void attachImage(@RequestParam("listingId") Long listingId, @RequestParam("image") MultipartFile image,
                            @AuthenticationPrincipal Jwt authenticationPrincipal) {
        logger.info("Attaching image to listing id: {}", listingId);
        imageService.attachImage(listingId, image, authenticationPrincipal.getSubject());
    }

    @Operation(summary = "Fetch image by path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image fetched successfully"),
    })
    @GetMapping("images")
    public ResponseEntity<?> fetchImage(@RequestBody ImageRequest imageRequest) {
        logger.info("Fetching image with path: {}", imageRequest.path());
        Image image = imageService.fetchImage(imageRequest.path());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(image.metadata().contentType()))
                .body(image.data());
    }

    @Operation(summary = "Fetch paths of listing images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paths of listing images fetched successfully")
    })
    @GetMapping("listings/{id}/images")
    public List<String> fetchListingImagesPaths(@PathVariable("id") Long listingId) {
        logger.info("Getting paths for listingId: {}", listingId);
        return imageService.fetchListingImagesPaths(listingId);
    }

    @ExceptionHandler(NotSupportedFileExtensionException.class)
    public ResponseEntity<String> handleNotSupportedFileExtensionException(NotSupportedFileExtensionException ex) {
        logger.error("Not supported file extension: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
