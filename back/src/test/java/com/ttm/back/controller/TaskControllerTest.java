package com.ttm.back.controller;

import com.ttm.back.AbstractApiTest;
import com.ttm.back.model.Role;
import com.ttm.back.model.User;
import com.ttm.back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest extends AbstractApiTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createTaskSucceedsWithValidPayload() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, SEEDED_PASSWORD);

        Map<String, Object> payload = Map.of(
                "projectId", 1,
                "title", "Write onboarding docs",
                "priority", "medium",
                "assignedUserId", 2
        );

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write onboarding docs"))
                .andExpect(jsonPath("$.status").value("todo"));
    }

    @Test
    void createTaskFailsValidationWhenTitleMissing() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, SEEDED_PASSWORD);

        Map<String, Object> payload = Map.of(
                "projectId", 1,
                "priority", "medium"
        );

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonMemberIsForbiddenFromViewingProjectTasks() throws Exception {
        User outsider = new User();
        outsider.setFirstName("Ivan");
        outsider.setLastName("Ilic");
        outsider.setEmail("ivan.ilic@example.com");
        outsider.setPasswordHash(passwordEncoder.encode("Outsider123!"));
        outsider.setRole(Role.member);
        outsider.setActive(true);
        userRepository.save(outsider);

        String token = loginAndGetToken("ivan.ilic@example.com", "Outsider123!");

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusChangeSucceedsAndIsRecordedInHistory() throws Exception {
        String token = loginAndGetToken(MEMBER_EMAIL, SEEDED_PASSWORD);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"status\":\"done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("done"));

        mockMvc.perform(get("/api/tasks/1/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("status"))
                .andExpect(jsonPath("$[0].newValue").value("done"));
    }
}
