package com.msn.MSNcars.image;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final String bucketName = "images";
    private final List<String> allowedContentTypes = List.of(".jpg", ".jpeg", ".png");
    private final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    public ImageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void attachImage(Long listingId, MultipartFile image) {
        //Bucket structure is flat, prefix is used to create hierarchy
        String prefix = "listing" + listingId + "/" + image.getOriginalFilename();
        logger.info("Created prefix for image: {}", prefix);

        String fileExtension = getFileExtension(image);
        if(!allowedContentTypes.contains(fileExtension)){
            logger.error("File extension {} not allowed", fileExtension);
            throw new NotSupportedFileExtensionException("File extenstion " + fileExtension + " not allowed");
        }

        try{
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(prefix)
                    .stream(image.getInputStream(), image.getSize(), -1)
                    .contentType(image.getContentType())
                    .build()
            );

            logger.info("Image successfully saved to database");
        } catch (MinioException e) {
            logger.error("Minio error occurred when trying to save image: {}", String.valueOf(e));
            logger.error("HTTP trace: {}", e.httpTrace());
        }catch (Exception e){
            logger.error("Exception occurred when trying to save image", e);
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
            logger.info("Image successfully retrieved from database");
            return new InputStreamResource(imageStream);
        } catch (MinioException e) {
            logger.error("Minio error occurred when trying to read image: {}", String.valueOf(e));
            logger.error("HTTP trace: {}", e.httpTrace());
        }catch (Exception e){
            logger.error("Exception occurred when trying to read image", e);
        }

        return null;
    }

    @Override
    public List<String> fetchListingImagesPaths(Long listingId) {
        List<String> imagePaths = new LinkedList<>();
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix("listing" + listingId + "/")
                    .build()
            );

            for (var object : objects) {
                imagePaths.add(object.get().objectName());
            }

            return imagePaths;
        } catch (MinioException e) {
            logger.error("Minio error occurred when trying to get objects under prefix: {}", String.valueOf(e));
            logger.error("HTTP trace: {}", e.httpTrace());
        }catch (Exception e){
            logger.error("Exception occurred when trying to get objects under prefix", e);
        }

        return null;
    }

    private static String getFileExtension(MultipartFile multipartFile){
        String filename = multipartFile.getOriginalFilename();
        return filename.substring(filename.lastIndexOf("."));
    }

    @PostConstruct
    public void createDefaultBucket() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            logger.info("Creating {} bucket", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            logger.info("Successfully created bucket");
        }
    }
}
