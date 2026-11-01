package com.tutorial.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Creates signed JWT access tokens for authenticated users. */
@Component
public class JwtTokenIssuer {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtTokenIssuer(JwtEncoder encoder, @Value("${app.security.jwt.ttl:PT1H}") Duration ttl) {
        this.encoder = encoder;
        this.ttl = ttl;
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }

    public String issue(Authentication authentication) {
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX)) // skip non-role authorities such as FACTOR_PASSWORD
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-boot-tutorial")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
