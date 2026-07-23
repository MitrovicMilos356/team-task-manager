package com.ttm.back.controller;

import com.ttm.back.AbstractApiTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTest extends AbstractApiTest {

    @Test
    void statisticsReflectSeededData() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, SEEDED_PASSWORD);

        mockMvc.perform(get("/api/dashboard/statistics").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.totalOpenTasks").value(2))
                .andExpect(jsonPath("$.overdueTasks").value(0))
                .andExpect(jsonPath("$.myAssignedOpenTasks").value(1));
    }
}
