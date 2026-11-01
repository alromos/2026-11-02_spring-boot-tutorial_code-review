package com.tutorial.application.port.out;

/** Lets the application report business events without knowing how they are measured. */
public interface CustomerMetricsPort {
    void customerCreated();

    void customerUpdated();

    void customerDeleted();
}
