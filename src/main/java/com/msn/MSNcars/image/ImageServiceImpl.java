package com.msn.MSNcars.image;

import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final String bucketName = "images";

    public ImageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void uploadImage(String prefix, MultipartFile image) {
        try{
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(prefix + "/" + image.getOriginalFilename())
                            .stream(image.getInputStream(), image.getSize(), -1)
                            .contentType(image.getContentType())
                            .build()
            );
        } catch (MinioException e) {
            System.out.println("Minio error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
