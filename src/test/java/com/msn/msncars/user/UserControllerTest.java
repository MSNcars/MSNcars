package com.msn.msncars.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msn.msncars.auth.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void blockingUsersShouldBeAllowedForAdminOnly() throws Exception {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                "testUser451",
                "testUser451",
                "testUser451@gmail.com",
                "John",
                "Mohn"
        );

        mockMvc.perform(post("/auth/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/user/testUser451@gmail.com/block")
                        .with(jwt().authorities(() -> "ROLE_user")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/user/testUser451@gmail.com/block")
                        .with(jwt().authorities(() -> "ROLE_company")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/user/testUser451@gmail.com/block")
                        .with(jwt().authorities(() -> "ROLE_admin")))
                .andExpect(status().isOk());
    }

}