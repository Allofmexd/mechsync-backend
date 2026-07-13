package com.mechsync.modules.auth.infrastructure.security;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.application.port.out.TokenGeneratorPort;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtService implements TokenGeneratorPort {

    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        byte[] keyBytes = Decoders.BASE64.decode(properties.secret());
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("MECHSYNC_JWT_SECRET must contain at least 256 bits");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public GeneratedToken generate(AuthenticatedUser user) {
        Instant issuedAt = clock.instant();
        Duration lifetime = Duration.ofMinutes(properties.expirationMinutes());
        Instant expiresAt = issuedAt.plus(lifetime);

        String token = Jwts.builder()
                .subject(user.email())
                .issuer(properties.issuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(USER_ID_CLAIM, user.id())
                .claim(ROLES_CLAIM, List.copyOf(user.roles()))
                .signWith(signingKey)
                .compact();

        return new GeneratedToken(token, lifetime.toSeconds());
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number userId = claims.get(USER_ID_CLAIM, Number.class);
        List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);
        Set<String> roles = new LinkedHashSet<>();
        if (rawRoles != null) {
            rawRoles.stream().map(String::valueOf).forEach(roles::add);
        }

        return new AuthenticatedUser(userId.longValue(), claims.getSubject(), roles);
    }
}
