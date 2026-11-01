package com.tutorial.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tutorial.domain.Customer;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository test: only the JPA slice is started. It uses the H2 database from the
 * "test" profile (replace = NONE), with the schema created by Flyway.
 * Each test runs in a transaction that is rolled back.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(CustomerPersistenceAdapter.class)
class CustomerPersistenceAdapterTest {

    @Autowired
    private CustomerPersistenceAdapter adapter;

    @Test
    void saveAssignsGeneratedId() {
        Customer saved = adapter.save(new Customer(null, "Ann", "ann@example.com"));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Ann");
        assertThat(saved.email()).isEqualTo("ann@example.com");
    }

    @Test
    void findByIdReturnsSavedCustomer() {
        Customer saved = adapter.save(new Customer(null, "Ann", "ann@example.com"));

        assertThat(adapter.findById(saved.id())).contains(saved);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(adapter.findById(123456L)).isEmpty();
    }

    @Test
    void saveWithExistingIdUpdatesTheRow() {
        Customer saved = adapter.save(new Customer(null, "Ann", "ann@example.com"));

        adapter.save(new Customer(saved.id(), "Anna", "anna@example.com"));

        assertThat(adapter.findById(saved.id())).contains(new Customer(saved.id(), "Anna", "anna@example.com"));
    }

    @Test
    void existsAndDelete() {
        Customer saved = adapter.save(new Customer(null, "Ann", "ann@example.com"));
        assertThat(adapter.existsById(saved.id())).isTrue();

        adapter.deleteById(saved.id());

        assertThat(adapter.existsById(saved.id())).isFalse();
    }

    @Test
    void findAllReturnsPagesSortedByIdWithTotals() {
        for (int i = 1; i <= 5; i++) {
            adapter.save(new Customer(null, "C" + i, "c" + i + "@example.com"));
        }

        PageResult<Customer> first = adapter.findAll(new PageQuery(0, 2));
        PageResult<Customer> last = adapter.findAll(new PageQuery(2, 2));

        assertThat(first.content()).extracting(Customer::name).containsExactly("C1", "C2");
        assertThat(first.totalElements()).isEqualTo(5);
        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(last.content()).extracting(Customer::name).containsExactly("C5");
    }

    @Test
    void findAllBeyondLastPageIsEmpty() {
        adapter.save(new Customer(null, "Ann", "ann@example.com"));

        PageResult<Customer> result = adapter.findAll(new PageQuery(5, 10));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(1);
    }
}
