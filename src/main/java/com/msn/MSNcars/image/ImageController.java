package com.msn.MSNcars.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /*
        TODO: Check if user owns the listing he is trying to add images to (after setting up authentication)
    */
    @PostMapping("images")
    @ResponseStatus(HttpStatus.CREATED)
    public void attachImage(@RequestParam("listingId") Long listingId, @RequestParam("image") MultipartFile image) {
        logger.info("Attaching image to listing id: {}", listingId);
        imageService.attachImage(listingId, image);
    }

    @GetMapping("images")
    public ResponseEntity<?> fetchImage(@RequestBody ImageRequest imageRequest) {
        logger.info("Fetching image with path: {}", imageRequest.path());
        Resource resource = imageService.fetchImage(imageRequest.path());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("listings/{id}/images")
    public List<String> fetchListingImagesPaths(@PathVariable("id") Long listingId) {
        logger.info("Getting paths for listingId: {}", listingId);
        return imageService.fetchListingImagesPaths(listingId);
    }

    @ExceptionHandler(NotSupportedFileExtensionException.class)
    public ResponseEntity<String> handleIllegalArgument(NotSupportedFileExtensionException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
