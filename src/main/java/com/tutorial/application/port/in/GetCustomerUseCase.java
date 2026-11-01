package com.tutorial.application.port.in;

import com.tutorial.domain.Customer;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;

public interface GetCustomerUseCase {
    Customer getById(Long id);

    PageResult<Customer> getAll(PageQuery query);

    long count();
}
