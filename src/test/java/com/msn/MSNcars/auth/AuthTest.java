package com.msn.MSNcars.auth;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Import(AuthConfig.class)
class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:80"; // Will be using RestAssured to send request to Nginx
    }

    @Test
    void testLoginToPredefinedAccount() throws Exception {
        // Use RestAssured to get access token
        Response loginResponse =
                given()
                        .contentType(ContentType.URLENC)
                        .formParam("grant_type", "password")
                        .formParam("client_id", "MSNcars")
                        .formParam("username", "user")
                        .formParam("password", "user")
                        .when()
                        .post("/auth/login")
                        .then()
                        .statusCode(200) // Ensure login is successful
                        .body("access_token", notNullValue()) // Ensure token is returned
                        .extract()
                        .response();

        String token = loginResponse.jsonPath().getString("access_token");

        //Test without token
        mockMvc.perform(get("/secure"))
                .andExpect(status().isUnauthorized());

        // Test with token
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearer " + token)
                ).andExpect(status().isOk());
    }


}