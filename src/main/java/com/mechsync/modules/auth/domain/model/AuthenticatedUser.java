package com.mechsync.modules.auth.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public record AuthenticatedUser(Long id, String email, Set<String> roles) {

    public AuthenticatedUser {
        roles = Collections.unmodifiableSet(new TreeSet<>(roles));
    }
}
