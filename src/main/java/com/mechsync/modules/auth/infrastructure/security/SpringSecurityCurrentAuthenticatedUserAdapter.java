package com.mechsync.modules.auth.infrastructure.security;

import com.mechsync.modules.auth.application.port.out.CurrentAuthenticatedUserPort;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentAuthenticatedUserAdapter implements CurrentAuthenticatedUserPort {

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)
                || authenticatedUser.id() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return authenticatedUser;
    }
}
