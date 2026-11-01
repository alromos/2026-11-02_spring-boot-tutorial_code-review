package com.tutorial.infrastructure.adapter.out.metrics;

import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.domain.PageQuery;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers a gauge with the current number of customers, read on every scrape. */
@Configuration
class CustomerGaugeConfig {

    @Bean
    MeterBinder customerCountGauge(ObjectProvider<GetCustomerUseCase> customers) {
        // ObjectProvider: the use case is looked up lazily, which avoids a startup dependency cycle.
        return registry -> Gauge
                .builder("customers.count", () -> customers.getObject().getAll(new PageQuery(0, 1)).totalElements())
                .description("Current number of customers")
                .register(registry);
    }
}
