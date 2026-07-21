package com.mechsync.modules.auth.application.port.out;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;

/** Provides the user established by the validated security context. */
public interface CurrentAuthenticatedUserPort {

    AuthenticatedUser getCurrentUser();
}
