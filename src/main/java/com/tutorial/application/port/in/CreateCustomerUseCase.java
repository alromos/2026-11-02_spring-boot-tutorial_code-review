package com.tutorial.application.port.in;

import com.tutorial.domain.Customer;

public interface CreateCustomerUseCase {
    Customer create(String name, String email);
}
