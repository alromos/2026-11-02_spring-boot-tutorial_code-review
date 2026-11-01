package com.tutorial.infrastructure.adapter.out;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Spring Boot tests switch metric export off by default; turn Prometheus on for this test.
@SpringBootTest(properties = "management.prometheus.metrics.export.enabled=true")
@AutoConfigureMockMvc
class ActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    private String bearer(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.access_token");
    }

    @Test
    void healthIsPublicButHidesDetailsFromAnonymousUsers() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void adminSeesCustomHealthIndicator() throws Exception {
        mockMvc.perform(get("/actuator/health").header("Authorization", bearer("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.customerStore.status").value("UP"))
                .andExpect(jsonPath("$.components.customerStore.details.customers").isNumber())
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    void prometheusEndpointRequiresAdmin() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/prometheus").header("Authorization", bearer("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customMetricsAreExposedToPrometheus() throws Exception {
        String admin = bearer("admin", "admin123");
        mockMvc.perform(post("/api/customers").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Metric\",\"email\":\"metric@example.com\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/actuator/prometheus").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("customers_operations_total{")))
                .andExpect(content().string(containsString("operation=\"create\"")))
                .andExpect(content().string(containsString("customers_count")));
    }
}
