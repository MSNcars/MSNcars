package com.msn.msncars.image;

import com.msn.msncars.listing.Listing;
import com.msn.msncars.listing.ListingRepository;
import com.msn.msncars.listing.ListingService;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final ListingService listingService;
    private final ListingRepository listingRepository;
    public static final String bucketName = "images";
    private final List<String> allowedContentTypes = List.of(".jpg", ".jpeg", ".png");
    private final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    public ImageServiceImpl(MinioClient minioClient, ListingService listingService, ListingRepository listingRepository) {
        this.minioClient = minioClient;
        this.listingService = listingService;
        this.listingRepository = listingRepository;
    }

    @Override
    public void attachImage(Long listingId, MultipartFile image, String userId) {
        //Bucket structure is flat, prefix is used to create hierarchy
        String prefix = createPrefix(listingId, image.getOriginalFilename());
        logger.info("Created prefix for image: {}", prefix);

        // Check that passed file is of type image
        String fileExtension = getFileExtension(image);
        if(!allowedContentTypes.contains(fileExtension)){
            logger.error("File extension {} not allowed", fileExtension);
            throw new NotSupportedFileExtensionException(String.format("File extension %s not allowed", fileExtension));
        }

        // Check that listing exists
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));
        listingService.validateListingOwnership(listing, userId);
        listingService.validateListingActive(listing);

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
    public Image fetchImage(String path){
        try{
            logger.info("Trying to retrieve image metadata from object storage");
            StatObjectResponse imageMetadata = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(path).build()
            );
            logger.info("Image metadata successfully retrieved from object storage");

            logger.info("Trying to retrieve image from object storage.");
            InputStream imageStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(path).build()
            );
            logger.info("Image successfully retrieved from object storage");

            return new Image(imageMetadata, new InputStreamResource(imageStream));
        } catch (MinioException e) {
            logger.error("Minio error occurred when trying to fetch image: {}", String.valueOf(e));
            logger.error("HTTP trace: {}", e.httpTrace());
        }catch (Exception e){
            logger.error("Exception occurred when trying to fetch image", e);
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
                    .prefix(createPrefix(listingId))
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

    private static String createPrefix(Long listingId){
        return createPrefix(listingId, "");
    }

    private static String createPrefix(Long listingId, String filename){
        return String.format("listing%d/%s", listingId, filename);
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
