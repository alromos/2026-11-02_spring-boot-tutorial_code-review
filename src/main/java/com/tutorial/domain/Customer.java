package com.tutorial.domain;

/** Customer domain model. Pure Java, no framework dependencies. */
public record Customer(Long id, String name, String email) {
}
