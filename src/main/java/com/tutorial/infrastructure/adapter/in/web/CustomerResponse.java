package com.tutorial.infrastructure.adapter.in.web;

import com.tutorial.domain.Customer;

public record CustomerResponse(Long id, String name, String email) {

    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.id(), customer.name(), customer.email());
    }
}
