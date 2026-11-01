# Spring Boot Tutorial: Tasks

Stack: Java 25 (LTS), Spring Boot 4.1.2, Maven, hexagonal architecture.

Dependency rule: `infrastructure -> application -> domain`. Never the reverse.

## Done
- [x] Setup: Spring Initializr skeleton and hexagonal package layout
- [x] Task 1 (15 pts): Hello-world application with `CommandLineRunner`
- [x] Task 2 (20 pts): Customer CRUD REST API (Spring Data JPA, H2, paginated list)
- [x] Task 3 (20 pts): OAuth2 resource server with JWT, USER/ADMIN roles
- [x] Task 4 (10 pts): Spring profiles local/dev/stg/prod with per-environment DB config
- [x] Task 5 (10 pts, optional): Flyway migrations, run automatically at startup
- [x] Task 6 (15 pts): Actuator, custom health indicator, Prometheus metrics
- [x] Task 7 (10 pts): Test suite (H2 test profile, repository, service, MockMvc, integration, end-to-end)

## Next
- [ ] Task 8: (waiting for the description)

## Notes
- Initializr returns `4.1.2.RELEASE`, which does not exist on Central. The correct version is `4.1.2`.
- `.mvn/maven.config` points Maven at `.mvn/settings.xml` (empty), so this project resolves from Maven Central only.
