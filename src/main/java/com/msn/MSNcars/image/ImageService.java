package com.msn.MSNcars.image;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface ImageService {
    void attachImage(Long listingId, MultipartFile image);
    Resource fetchImage(String path);
    List<String> fetchListingImagesPaths(Long listingId);
}
