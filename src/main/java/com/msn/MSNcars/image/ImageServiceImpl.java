package com.msn.MSNcars.image;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final String bucketName = "images";
    private final List<String> allowedContentTypes = List.of(".jpg", ".jpeg", ".png");

    public ImageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void attachImage(Long listingId, MultipartFile image) {
        //Bucket structure is flat, prefix is used to create hierarchy
        String prefix = "listing" + listingId + "/" + image.getOriginalFilename();

        if(!allowedContentTypes.contains(getFileExtenstion(image)))
            return;//throw custom exception here

        try{
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(prefix)
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

    @Override
    public Resource fetchImage(String path){
        try{
            InputStream imageStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build()
            );
            return new InputStreamResource(imageStream);
        } catch (MinioException e) {
            System.out.println("Minio error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<String> fetchListingImagesPath(Long listingId) {
        List<String> imagePaths = new LinkedList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix("listing" + listingId + "/")
                            .build()
            );

            for (var result : results) {
                imagePaths.add(result.get().objectName());
            }

            return imagePaths;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getFileExtenstion(MultipartFile multipartFile){
        String filename = multipartFile.getOriginalFilename();
        return filename.substring(filename.lastIndexOf("."));
    }

    @PostConstruct
    public void createDefaultBucket() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
