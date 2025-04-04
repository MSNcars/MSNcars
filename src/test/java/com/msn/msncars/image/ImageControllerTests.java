package com.msn.msncars.image;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
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

    @Container
    private final static MinIOContainer container = new MinIOContainer("minio/minio")
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
    public void testThatAfterSavingImageYouCanFetchIt() throws Exception {
        Resource jpegPhoto = new ClassPathResource("jpegTestPhoto.jpg");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", "33")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isCreated());

        var fetchResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ImageRequest("/listing33/jpegTestPhoto.jpg")))
        ).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
            .andReturn()
            .getResponse();

        assertEquals(jpegPhoto.getContentAsString(StandardCharsets.UTF_8), fetchResult.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void testThatYouCannotSaveFileWithBadExtension() throws Exception{
        Resource jpegPhoto = new ClassPathResource("bmpTestPhoto.bmp");

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
    public void testThatYouGetCorrectPathsForListing() throws Exception{
        Resource jpegPhoto = new ClassPathResource("jpegTestPhoto.jpg");
        Resource pngPhoto = new ClassPathResource("pngTestPhoto.png");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());
        MockMultipartFile mockPngPhoto = new MockMultipartFile("image", pngPhoto.getFilename(), MediaType.IMAGE_PNG_VALUE,
                pngPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", "35")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isCreated());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockPngPhoto)
                .param("listingId", "35")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isCreated());

        mockMvc.perform(
            MockMvcRequestBuilders.get("/listings/{id}/images", 35)
        ).andExpect(content().json(objectMapper.writeValueAsString(
            List.of("listing35/jpegTestPhoto.jpg", "listing35/pngTestPhoto.png")
        )));
    }

    /* This test will be implemented after adding authentication
    @Test
    public void testThatYouCannotAddImagesToOtherPeopleListings(){

    }
     */

    @AfterEach
    public void tearDown() throws Exception{
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