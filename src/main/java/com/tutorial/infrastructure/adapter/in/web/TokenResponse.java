package com.tutorial.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/** OAuth2-style access token response (RFC 6749, section 5.1). */
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn) {
}
