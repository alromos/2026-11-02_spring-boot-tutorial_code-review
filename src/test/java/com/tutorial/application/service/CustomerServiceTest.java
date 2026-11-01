package com.tutorial.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tutorial.application.port.out.CustomerMetricsPort;
import com.tutorial.application.port.out.CustomerRepositoryPort;
import com.tutorial.domain.Customer;
import com.tutorial.domain.CustomerNotFoundException;
import com.tutorial.domain.DuplicateEmailException;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pure unit test: no Spring, no database. Ports are replaced with mocks. */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort repository;

    @Mock
    private CustomerMetricsPort metrics;

    @InjectMocks
    private CustomerService service;

    @Test
    void createSavesNewCustomerWithoutIdAndReportsMetric() {
        when(repository.save(new Customer(null, "Ann", "ann@example.com")))
                .thenReturn(new Customer(1L, "Ann", "ann@example.com"));

        Customer created = service.create("Ann", "ann@example.com");

        assertThat(created.id()).isEqualTo(1L);
        verify(metrics).customerCreated();
    }

    @Test
    void getByIdReturnsCustomer() {
        Customer ann = new Customer(1L, "Ann", "ann@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(ann));

        assertThat(service.getById(1L)).isEqualTo(ann);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllDelegatesPagingToRepository() {
        PageQuery query = new PageQuery(1, 5);
        PageResult<Customer> page = new PageResult<>(List.of(), 1, 5, 0, 0);
        when(repository.findAll(query)).thenReturn(page);

        assertThat(service.getAll(query)).isSameAs(page);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("ann@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create("Ann", "Ann@Example.com"))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).save(any());
        verifyNoInteractions(metrics);
    }

    @Test
    void createStoresEmailInLowerCase() {
        when(repository.save(new Customer(null, "Ann", "ann@example.com")))
                .thenReturn(new Customer(1L, "Ann", "ann@example.com"));

        assertThat(service.create("Ann", " Ann@Example.COM ").email()).isEqualTo("ann@example.com");
    }

    @Test
    void updateKeepsIdAndReportsMetric() {
        when(repository.update(new Customer(1L, "Anna", "anna@example.com")))
                .thenReturn(Optional.of(new Customer(1L, "Anna", "anna@example.com")));

        Customer updated = service.update(1L, "Anna", "anna@example.com");

        assertThat(updated.name()).isEqualTo("Anna");
        verify(metrics).customerUpdated();
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.update(new Customer(99L, "X", "x@example.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, "X", "x@example.com"))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(repository, never()).save(any());
        verifyNoInteractions(metrics);
    }

    @Test
    void updateRejectsEmailOfAnotherCustomer() {
        when(repository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, "Anna", "taken@example.com"))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).update(any());
        verifyNoInteractions(metrics);
    }

    @Test
    void deleteRemovesCustomerAndReportsMetric() {
        when(repository.deleteById(1L)).thenReturn(true);

        service.delete(1L);

        verify(metrics).customerDeleted();
    }

    @Test
    void deleteThrowsAndDeletesNothingWhenMissing() {
        when(repository.deleteById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(CustomerNotFoundException.class);

        verifyNoInteractions(metrics);
    }
}
