package com.msn.MSNcars.image;

import org.springframework.web.multipart.MultipartFile;
public interface ImageService {
    void uploadImage(String prefix, MultipartFile image);
}
