package com.mechsync.modules.auth.infrastructure.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mechsync.security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Min(1) long expirationMinutes,
        @NotBlank String issuer) {
}
