package com.tutorial.application.port.in;

import com.tutorial.domain.Customer;

public interface UpdateCustomerUseCase {
    Customer update(Long id, String name, String email);
}
