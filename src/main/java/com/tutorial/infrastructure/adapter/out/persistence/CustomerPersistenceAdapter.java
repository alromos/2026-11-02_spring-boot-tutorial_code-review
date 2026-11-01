package com.tutorial.infrastructure.adapter.out.persistence;

import com.tutorial.application.port.out.CustomerRepositoryPort;
import com.tutorial.domain.Customer;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Driven adapter: implements the repository port using Spring Data JPA. */
@Component
class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository jpaRepository;

    CustomerPersistenceAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(CustomerJpaEntity.fromDomain(customer)).toDomain();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(CustomerJpaEntity::toDomain);
    }

    @Override
    public PageResult<Customer> findAll(PageQuery query) {
        // Sort by id so that paging is stable between requests.
        Page<CustomerJpaEntity> page = jpaRepository.findAll(
                PageRequest.of(query.page(), query.size(), Sort.by("id")));
        return new PageResult<>(
                page.getContent().stream().map(CustomerJpaEntity::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
