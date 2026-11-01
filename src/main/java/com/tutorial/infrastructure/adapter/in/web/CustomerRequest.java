package com.tutorial.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "name must not be blank") String name,
        @NotBlank(message = "email must not be blank") @Email(message = "email must be valid") String email) {
}
