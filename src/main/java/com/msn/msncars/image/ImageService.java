package com.msn.msncars.image;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface ImageService {
    void attachImage(Long listingId, MultipartFile image, String userId);
    Image fetchImage(String path);
    List<String> fetchListingImagesPaths(Long listingId);
}
