package com.tutorial.infrastructure.adapter.in.web;

import com.tutorial.domain.Customer;
import com.tutorial.domain.PageResult;
import java.util.List;

public record CustomerPageResponse(List<CustomerResponse> content, int page, int size,
                                   long totalElements, int totalPages) {

    static CustomerPageResponse from(PageResult<Customer> result) {
        return new CustomerPageResponse(
                result.content().stream().map(CustomerResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
