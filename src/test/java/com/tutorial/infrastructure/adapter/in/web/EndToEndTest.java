package com.tutorial.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

/**
 * End-to-end test: the real application on a random port, real HTTP calls,
 * real security filter chain, real Flyway-migrated H2 database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndToEndTest {

    @LocalServerPort
    private int port;

    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    private String login(String username, String password) {
        Map<String, Object> token = client().post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", password))
                .retrieve().body(new ParameterizedTypeReference<>() { });
        return "Bearer " + token.get("access_token");
    }

    private HttpStatusCode statusOf(String method, String uri, String auth) {
        return client().method(org.springframework.http.HttpMethod.valueOf(method)).uri(uri)
                .headers(h -> {
                    if (auth != null) {
                        h.set("Authorization", auth);
                    }
                })
                .exchange((req, res) -> res.getStatusCode());
    }

    @Test
    void adminCanRunAWholeCustomerLifecycleOverHttp() {
        String admin = login("admin", "admin123");

        Map<String, Object> created = client().post().uri("/api/customers")
                .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", "E2E", "email", "e2e@example.com"))
                .retrieve().body(new ParameterizedTypeReference<>() { });
        Object id = created.get("id");
        assertThat(id).isNotNull();

        Map<String, Object> read = client().get().uri("/api/customers/{id}", id)
                .header("Authorization", admin).retrieve().body(new ParameterizedTypeReference<>() { });
        assertThat(read).containsEntry("name", "E2E");

        Map<String, Object> page = client().get().uri("/api/customers?page=0&size=10")
                .header("Authorization", admin).retrieve().body(new ParameterizedTypeReference<>() { });
        assertThat((List<?>) page.get("content")).isNotEmpty();

        client().put().uri("/api/customers/{id}", id).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", "E2E-changed", "email", "e2e@example.com"))
                .retrieve().toBodilessEntity();

        assertThat(statusOf("DELETE", "/api/customers/" + id, admin).value()).isEqualTo(204);
        assertThat(statusOf("GET", "/api/customers/" + id, admin).value()).isEqualTo(404);
    }

    @Test
    void securityRulesAreEnforcedOverHttp() {
        String user = login("user", "user123");

        assertThat(statusOf("GET", "/api/customers", null).value()).isEqualTo(401);
        assertThat(statusOf("GET", "/api/customers", user).value()).isEqualTo(200);
        assertThat(statusOf("DELETE", "/api/customers/1", user).value()).isEqualTo(403);
        assertThat(statusOf("GET", "/actuator/health", null).value()).isEqualTo(200);
    }
}
