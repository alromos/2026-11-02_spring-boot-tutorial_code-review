package com.tutorial.infrastructure.adapter.in.web;

import com.tutorial.application.port.in.CreateCustomerUseCase;
import com.tutorial.application.port.in.DeleteCustomerUseCase;
import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.application.port.in.UpdateCustomerUseCase;
import com.tutorial.domain.Customer;
import com.tutorial.domain.PageQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Driving adapter: exposes customer use cases over HTTP. */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomer;
    private final GetCustomerUseCase getCustomer;
    private final UpdateCustomerUseCase updateCustomer;
    private final DeleteCustomerUseCase deleteCustomer;

    public CustomerController(CreateCustomerUseCase createCustomer, GetCustomerUseCase getCustomer,
                              UpdateCustomerUseCase updateCustomer, DeleteCustomerUseCase deleteCustomer) {
        this.createCustomer = createCustomer;
        this.getCustomer = getCustomer;
        this.updateCustomer = updateCustomer;
        this.deleteCustomer = deleteCustomer;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer created = createCustomer.create(request.name(), request.email());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(CustomerResponse.from(created));
    }

    @GetMapping
    public CustomerPageResponse getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return CustomerPageResponse.from(getCustomer.getAll(new PageQuery(page, size)));
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return CustomerResponse.from(getCustomer.getById(id));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return CustomerResponse.from(updateCustomer.update(id, request.name(), request.email()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteCustomer.delete(id);
        return ResponseEntity.noContent().build();
    }
}
