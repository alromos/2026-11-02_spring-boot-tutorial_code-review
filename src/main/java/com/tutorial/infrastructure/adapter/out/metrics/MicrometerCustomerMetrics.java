package com.tutorial.infrastructure.adapter.out.metrics;

import com.tutorial.application.port.out.CustomerMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Driven adapter: counts customer operations with Micrometer.
 * In Prometheus: customers_operations_total{operation="create|update|delete"}.
 */
@Component
class MicrometerCustomerMetrics implements CustomerMetricsPort {

    private final Counter created;
    private final Counter updated;
    private final Counter deleted;

    MicrometerCustomerMetrics(MeterRegistry registry) {
        this.created = counter(registry, "create");
        this.updated = counter(registry, "update");
        this.deleted = counter(registry, "delete");
    }

    private static Counter counter(MeterRegistry registry, String operation) {
        return Counter.builder("customers.operations")
                .description("Number of customer operations performed")
                .tag("operation", operation)
                .register(registry);
    }

    @Override
    public void customerCreated() {
        created.increment();
    }

    @Override
    public void customerUpdated() {
        updated.increment();
    }

    @Override
    public void customerDeleted() {
        deleted.increment();
    }
}
