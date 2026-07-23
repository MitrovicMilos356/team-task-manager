package com.ttm.back.controller;

import com.ttm.back.AbstractApiTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractApiTest {

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        String body = objectMapper.writeValueAsString(new Object() {
            public final String email = ADMIN_EMAIL;
            public final String password = SEEDED_PASSWORD;
        });

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(ADMIN_EMAIL));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(new Object() {
            public final String email = ADMIN_EMAIL;
            public final String password = "not-the-right-password";
        });

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }
}
