package com.tutorial.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Verifies which configuration each profile applies. A bare context is started
 * (no auto-configuration), so no database connection is needed.
 */
class ProfileConfigTest {

    @Configuration
    static class EmptyConfig {
    }

    private static ConfigurableApplicationContext start(String... args) {
        return new SpringApplicationBuilder(EmptyConfig.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Test
    void localIsTheDefaultProfile() {
        try (ConfigurableApplicationContext ctx = start()) {
            Environment env = ctx.getEnvironment();
            assertThat(env.getActiveProfiles()).isEmpty();
            assertThat(env.getDefaultProfiles()).contains("local");
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:h2:mem:tutorial");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
        }
    }

    @Test
    void devUsesFileBasedH2() {
        try (ConfigurableApplicationContext ctx = start("--spring.profiles.active=dev")) {
            Environment env = ctx.getEnvironment();
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:h2:file:./data/tutorial-dev");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
        }
    }

    @Test
    void stgUsesPostgresFromEnvironmentVariables() {
        try (ConfigurableApplicationContext ctx = start("--spring.profiles.active=stg",
                "--DB_URL=jdbc:postgresql://stg-host:5432/app", "--DB_USERNAME=stg_user", "--DB_PASSWORD=secret")) {
            Environment env = ctx.getEnvironment();
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://stg-host:5432/app");
            assertThat(env.getProperty("spring.datasource.username")).isEqualTo("stg_user");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
        }
    }

    @Test
    void prodUsesPostgresAndNeverChangesSchema() {
        try (ConfigurableApplicationContext ctx = start("--spring.profiles.active=prod",
                "--DB_URL=jdbc:postgresql://prod-host:5432/app", "--DB_USERNAME=prod_user", "--DB_PASSWORD=secret")) {
            Environment env = ctx.getEnvironment();
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://prod-host:5432/app");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            assertThat(env.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo("20");
        }
    }

    @Test
    void prodFailsFastWhenDatabaseVariablesAreMissing() {
        try (ConfigurableApplicationContext ctx = start("--spring.profiles.active=prod")) {
            assertThatThrownBy(() -> ctx.getEnvironment().getProperty("spring.datasource.url"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DB_URL");
        }
    }
}
