package com.mechsync.modules.auth.infrastructure.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(UTF_8));
    private static final Instant ISSUED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void generatesAndParsesTokenWithRoles() {
        JwtService jwtService = serviceAt(ISSUED_AT);
        AuthenticatedUser user = new AuthenticatedUser(
                42L, "admin@example.com", Set.of("ADMINISTRADOR", "TECNICO"));

        GeneratedToken generatedToken = jwtService.generate(user);
        AuthenticatedUser parsedUser = jwtService.parse(generatedToken.value());

        assertEquals(7200, generatedToken.expiresInSeconds());
        assertEquals(user, parsedUser);
    }

    @Test
    void rejectsExpiredToken() {
        AuthenticatedUser user = new AuthenticatedUser(
                42L, "admin@example.com", Set.of("ADMINISTRADOR"));
        String token = serviceAt(ISSUED_AT).generate(user).value();

        JwtService expiredTokenService = serviceAt(ISSUED_AT.plusSeconds(7201));

        assertThrows(ExpiredJwtException.class, () -> expiredTokenService.parse(token));
    }

    @Test
    void rejectsInvalidToken() {
        JwtService jwtService = serviceAt(ISSUED_AT);

        assertThrows(JwtException.class, () -> jwtService.parse("not-a-jwt"));
    }

    private JwtService serviceAt(Instant instant) {
        JwtProperties properties = new JwtProperties(SECRET, 120, "mechsync-backend");
        return new JwtService(properties, Clock.fixed(instant, ZoneOffset.UTC));
    }
}
