package com.msn.MSNcars.image;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("images/upload")
    public void uploadImage(@RequestParam("image") MultipartFile image) {
        imageService.uploadImage("abcd", image);
    }
}
