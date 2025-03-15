package com.msn.MSNcars.image;

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

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("images")
    public void attachImage(@RequestParam("listingId") Long listingId, @RequestParam("image") MultipartFile image) {
        imageService.attachImage(listingId, image);
    }

    @GetMapping("images")
    public ResponseEntity<?> fetchImage(@RequestBody ImageRequest imageRequest) {
        Resource resource = imageService.fetchImage(imageRequest.path());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("listings/{id}/images")
    public List<String> fetchListingImagesPath(@PathVariable("id") Long listingId) {
        return imageService.fetchListingImagesPath(listingId);
    }
}
