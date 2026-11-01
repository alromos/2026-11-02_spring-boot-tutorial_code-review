package com.tutorial.application.service;

import com.tutorial.application.port.in.CreateCustomerUseCase;
import com.tutorial.application.port.in.DeleteCustomerUseCase;
import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.application.port.in.UpdateCustomerUseCase;
import com.tutorial.application.port.out.CustomerMetricsPort;
import com.tutorial.application.port.out.CustomerRepositoryPort;
import com.tutorial.domain.Customer;
import com.tutorial.domain.CustomerNotFoundException;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
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
        Customer saved = repository.save(new Customer(null, name, email));
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
    public Customer update(Long id, String name, String email) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        Customer saved = repository.save(new Customer(id, name, email));
        metrics.customerUpdated();
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        repository.deleteById(id);
        metrics.customerDeleted();
    }
}
