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

    /** Updates an existing customer. Empty if no customer with that id exists (it is never re-created). */
    Optional<Customer> update(Customer customer);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long count();

    /** @return true if a customer was deleted, false if none existed */
    boolean deleteById(Long id);
}
