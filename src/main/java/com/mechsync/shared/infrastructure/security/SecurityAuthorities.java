package com.mechsync.shared.infrastructure.security;

import com.mechsync.shared.domain.constant.SystemRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityAuthorities {

    public static final String ROLE_ADMINISTRADOR = "ROLE_ADMINISTRADOR";
    public static final String ROLE_TECNICO = "ROLE_TECNICO";
    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    private SecurityAuthorities() {
    }

    public static SimpleGrantedAuthority fromJwtRole(String role) {
        return new SimpleGrantedAuthority(SystemRole.fromJwtClaim(role).authority());
    }
}
