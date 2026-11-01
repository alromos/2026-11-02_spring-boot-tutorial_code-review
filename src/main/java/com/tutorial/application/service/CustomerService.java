package com.tutorial.application.service;

import com.tutorial.application.port.in.CreateCustomerUseCase;
import com.tutorial.application.port.in.DeleteCustomerUseCase;
import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.application.port.in.UpdateCustomerUseCase;
import com.tutorial.application.port.out.CustomerMetricsPort;
import com.tutorial.application.port.out.CustomerRepositoryPort;
import com.tutorial.domain.Customer;
import com.tutorial.domain.CustomerNotFoundException;
import com.tutorial.domain.DuplicateEmailException;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implements all customer use cases. Depends only on the domain and ports. */
@Service
@Transactional
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase,
        UpdateCustomerUseCase, DeleteCustomerUseCase {

    private final CustomerRepositoryPort repository;
    private final CustomerMetricsPort metrics;

    public CustomerService(CustomerRepositoryPort repository, CustomerMetricsPort metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    @Override
    public Customer create(String name, String email) {
        String normalized = normalize(email);
        if (repository.existsByEmail(normalized)) {
            throw new DuplicateEmailException(normalized);
        }
        Customer saved = repository.save(new Customer(null, name, normalized));
        metrics.customerCreated();
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Customer> getAll(PageQuery query) {
        return repository.findAll(query);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Override
    public Customer update(Long id, String name, String email) {
        String normalized = normalize(email);
        if (repository.existsByEmailAndIdNot(normalized, id)) {
            throw new DuplicateEmailException(normalized);
        }
        // update() loads and modifies the existing row, so a customer deleted in the meantime
        // is reported as not found instead of being silently re-created.
        Customer saved = repository.update(new Customer(id, name, normalized))
                .orElseThrow(() -> new CustomerNotFoundException(id));
        metrics.customerUpdated();
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw new CustomerNotFoundException(id);
        }
        metrics.customerDeleted();
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
