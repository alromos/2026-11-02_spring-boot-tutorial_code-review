package com.tutorial.infrastructure.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CustomerApiIntegrationTest {

    private static final String JSON = "{\"name\":\"%s\",\"email\":\"%s\"}";

    @Autowired
    private MockMvc mockMvc;

    private String admin;

    @BeforeEach
    void login() throws Exception {
        admin = TestTokens.bearer(mockMvc, "admin", "admin123");
    }

    private String createCustomer(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.formatted(name, email)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        return result.getResponse().getHeader("Location");
    }

    @Test
    void fullCrudCycle() throws Exception {
        String location = createCustomer("Ann", "ann@example.com");

        mockMvc.perform(get(location).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ann@example.com"));

        mockMvc.perform(put(location)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.formatted("Anna", "anna@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna"));

        mockMvc.perform(get("/api/customers").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name=='Anna')]").exists());

        mockMvc.perform(delete(location).header("Authorization", admin)).andExpect(status().isNoContent());
        mockMvc.perform(get(location).header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void listIsPaginated() throws Exception {
        for (int i = 0; i < 3; i++) {
            createCustomer("Paged" + i, "paged" + i + "@example.com");
        }

        mockMvc.perform(get("/api/customers").header("Authorization", admin).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalPages").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void invalidPagingParametersReturn400() throws Exception {
        mockMvc.perform(get("/api/customers").header("Authorization", admin).param("size", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/customers").header("Authorization", admin).param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/customers").header("Authorization", admin).param("page", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.formatted("", "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/customers/999999").header("Authorization", admin)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/customers/999999").header("Authorization", admin)).andExpect(status().isNotFound());
    }
}
