package com.msn.msncars.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.msn.msncars.car.*;
import com.msn.msncars.car.make.Make;
import com.msn.msncars.car.make.MakeRepository;
import com.msn.msncars.car.model.Model;
import com.msn.msncars.car.model.ModelRepository;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyRepository;
import com.msn.msncars.listing.Listing;
import com.msn.msncars.listing.ListingRepository;
import com.msn.msncars.listing.OwnerType;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.msn.msncars.image.ImageServiceImpl.bucketName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ImageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MinioClient minioClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private Clock clock;

    Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T23:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant()).thenReturn(fixedClock.instant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Container
    private static final MinIOContainer container = new MinIOContainer("minio/minio")
            .withUserName("minioadmintest")
            .withPassword("minioadmintest")
            .withCreateContainerCmdModifier(cmd ->
                //do port binding manually, so that I know endpoint for minioclient without having to call container.getS3URL()
                cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(9002), new ExposedPort(9000)),
                        new PortBinding(Ports.Binding.bindPort(9003), new ExposedPort(9001))
                    )
                )
            );

    @Test
    void testThatAfterAddingListingYouCanSaveAndFetchImageAsUserOwner() throws Exception {
        Long listingId = addSampleUserListing();

        Resource jpegPhoto = new ClassPathResource("/images/jpegTestPhoto.jpg");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/images")
                        .file(mockJpegPhoto)
                        .param("listingId", listingId.toString())
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getJwtForUserId(44L)))
        ).andExpect(status().isCreated());

        var fetchResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new ImageRequest(String.format("/listing%d/jpegTestPhoto.jpg", listingId))))
                ).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
                .andReturn()
                .getResponse();

        assertEquals(jpegPhoto.getContentAsString(StandardCharsets.UTF_8), fetchResult.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void testThatAfterAddingListingYouCanSaveAndFetchImageAsCompanyMember() throws Exception {
        Long listingId = addSampleCompanyListing();

        Resource jpegPhoto = new ClassPathResource("/images/jpegTestPhoto.jpg");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", listingId.toString())
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getJwtForUserId(2L)))
        ).andExpect(status().isCreated());

        var fetchResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ImageRequest(String.format("/listing%d/jpegTestPhoto.jpg", listingId))))
        ).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
            .andReturn()
            .getResponse();

        assertEquals(jpegPhoto.getContentAsString(StandardCharsets.UTF_8), fetchResult.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void testThatYouCannotSaveFileWithBadExtension() throws Exception{
        Resource jpegPhoto = new ClassPathResource("/images/bmpTestPhoto.bmp");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), "image/bmp",
                jpegPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", "34")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testThatYouGetCorrectPathsForListing() throws Exception{
        Long listingId = addSampleUserListing();

        Resource jpegPhoto = new ClassPathResource("/images/jpegTestPhoto.jpg");
        Resource pngPhoto = new ClassPathResource("/images/pngTestPhoto.png");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());
        MockMultipartFile mockPngPhoto = new MockMultipartFile("image", pngPhoto.getFilename(), MediaType.IMAGE_PNG_VALUE,
                pngPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", listingId.toString())
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getJwtForUserId(44L)))
        ).andExpect(status().isCreated());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockPngPhoto)
                .param("listingId", listingId.toString())
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getJwtForUserId(44L)))
        ).andExpect(status().isCreated());

        mockMvc.perform(
            MockMvcRequestBuilders.get("/listings/{id}/images", listingId)
        ).andExpect(content().json(objectMapper.writeValueAsString(
            List.of(
                    String.format("listing%d/jpegTestPhoto.jpg", listingId),
                    String.format("listing%d/pngTestPhoto.png", listingId)
            )
        )));
    }

    @Test
    void testThatYouCannotAddImagesToOtherPeopleListings() throws Exception {
        Long listingId = addSampleUserListing();

        Resource jpegPhoto = new ClassPathResource("/images/jpegTestPhoto.jpg");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/images")
                        .file(mockJpegPhoto)
                        .param("listingId", listingId.toString())
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getJwtForUserId(45L)))
        ).andExpect(status().isForbidden());
    }

    // Methods used for creating sample input data

    private Jwt getJwtForUserId(Long userId){
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();
    }

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    MakeRepository makeRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    FeatureRepository featureRepository;

    private Long addSampleUserListing(){
        Make bmw = new Make(2L, "BMW");

        Model series3 = new Model(2L, "Series 3", bmw);

        Feature navigation = new Feature(null, "Navigation");

        Listing listing1 = new Listing(
                null,
                "44",
                OwnerType.USER,
                series3,
                List.of(navigation),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("14000.00"),
                2008,
                49000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Good condition."
        );

        makeRepository.save(bmw);
        modelRepository.save(series3);
        featureRepository.save(navigation);
        return listingRepository.save(listing1).getId();
    }

    private Long addSampleCompanyListing(){
        Make toyota = new Make(1L, "Toyota");
        Model corolla = new Model(1L, "Corolla", toyota);

        Company autoWorld = new Company(null,
                "1",
                "Auto World",
                "123 Main St",
                "123-456-789",
                "contact@autoworld.com");
        autoWorld.addMember("1");
        autoWorld.addMember("2");

        Long companyId = companyRepository.save(autoWorld).getId();


        Feature sunroof = new Feature(null, "Sunroof");

        Listing listing1 = new Listing(
                null,
                companyId.toString(),
                OwnerType.COMPANY,
                corolla,
                List.of(sunroof),
                fixedClock.instant(),
                fixedClock.instant().plus(31, ChronoUnit.DAYS),
                false,
                new BigDecimal("18000.00"),
                2020,
                45000,
                Fuel.PETROL,
                CarUsage.USED,
                CarOperationalStatus.WORKING,
                CarType.SEDAN,
                "Well maintained Toyota Corolla with sunroof and nav."
        );

        makeRepository.save(toyota);
        modelRepository.save(corolla);
        featureRepository.save(sunroof);
        return listingRepository.save(listing1).getId();
    }

    @AfterEach
    void tearDown() throws Exception{
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build()
        );

        for(var object: objects){
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(object.get().objectName()).build()
            );
        }
    }

}