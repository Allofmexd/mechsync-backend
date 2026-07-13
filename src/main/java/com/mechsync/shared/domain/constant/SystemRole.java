package com.mechsync.shared.domain.constant;

/** Roles definidos por el catálogo {@code roles} de MechSync. */
public enum SystemRole {
    ADMINISTRADOR,
    TECNICO,
    CLIENTE;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + name();
    }

    public static SystemRole fromJwtClaim(String role) {
        if (role == null || role.isBlank() || role.startsWith(AUTHORITY_PREFIX)) {
            throw new IllegalArgumentException("Invalid role claim");
        }
        try {
            return valueOf(role);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid role claim", exception);
        }
    }
}
