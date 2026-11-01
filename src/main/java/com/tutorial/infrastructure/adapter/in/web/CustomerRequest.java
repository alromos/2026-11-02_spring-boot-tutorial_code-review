package com.tutorial.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "name must not be blank") @Size(max = 255, message = "name must be at most 255 characters") String name,
        @NotBlank(message = "email must not be blank") @Email(message = "email must be valid")
        @Size(max = 255, message = "email must be at most 255 characters") String email) {
}
