package com.tutorial.infrastructure.adapter.out.persistence;

import com.tutorial.domain.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA representation of a customer. Kept separate from the domain model. */
@Entity
@Table(name = "customers")
class CustomerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    protected CustomerJpaEntity() {
        // required by JPA
    }

    CustomerJpaEntity(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    static CustomerJpaEntity fromDomain(Customer customer) {
        return new CustomerJpaEntity(customer.id(), customer.name(), customer.email());
    }

    Customer toDomain() {
        return new Customer(id, name, email);
    }
}
