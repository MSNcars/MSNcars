package com.msn.msncars.user;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void blockingUsersShouldBeAllowedForAdminOnly() throws Exception {
        mockMvc.perform(patch("/user/123/block")
                        .with(jwt().authorities(() -> "ROLE_user")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/user/123/block")
                        .with(jwt().authorities(() -> "ROLE_company")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/user/123/block")
                        .with(jwt().authorities(() -> "ROLE_admin")))
                .andExpect(status().isNotFound());
    }

}