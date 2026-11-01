package com.tutorial.application.port.out;

import com.tutorial.domain.Customer;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import java.util.Optional;

/** What the application needs from a storage mechanism. */
public interface CustomerRepositoryPort {
    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    PageResult<Customer> findAll(PageQuery query);

    boolean existsById(Long id);

    void deleteById(Long id);
}
