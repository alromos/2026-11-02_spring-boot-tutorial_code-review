package com.tutorial.infrastructure.adapter.out.health;

import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.domain.PageQuery;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Application-specific health check: can the customer store actually be queried?
 * Appears as "customerStore" under /actuator/health.
 */
@Component("customerStore")
class CustomerStoreHealthIndicator implements HealthIndicator {

    private final GetCustomerUseCase customers;

    CustomerStoreHealthIndicator(GetCustomerUseCase customers) {
        this.customers = customers;
    }

    @Override
    public Health health() {
        try {
            long total = customers.getAll(new PageQuery(0, 1)).totalElements();
            return Health.up().withDetail("customers", total).build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
