package com.msn.MSNcars.image;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class ImagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testThatAfterSavingImageYouCanFetchIt() throws Exception {
        Resource jpegPhoto = new ClassPathResource("jpegTestPhoto.jpg");

        MockMultipartFile mockJpegPhoto = new MockMultipartFile("image", jpegPhoto.getFilename(), MediaType.IMAGE_JPEG_VALUE,
                jpegPhoto.getInputStream());

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/images")
                .file(mockJpegPhoto)
                .param("listingId", "33")
        ).andExpect(status().isCreated());

        var fetchResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new ImageRequest("/listing33/jpegTestPhoto.jpg")))
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
        ).andExpect(status().isCreated());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/images")
                        .file(mockPngPhoto)
                        .param("listingId", "35")
        ).andExpect(status().isCreated());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/listings/{id}/images", 35)
        ).andExpect(content().json(new ObjectMapper().writeValueAsString(
                List.of("listing35/jpegTestPhoto.jpg", "listing35/pngTestPhoto.png")
        )));
    }

    /* This test will be implemented after adding authentication
    @Test
    public void testThatYouCannnotAddImagesToOtherPeopleListings(){

    }
     */

}