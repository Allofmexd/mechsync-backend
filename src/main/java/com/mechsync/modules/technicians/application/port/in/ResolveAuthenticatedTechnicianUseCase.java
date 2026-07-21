package com.mechsync.modules.technicians.application.port.in;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.technicians.domain.model.Technician;

public interface ResolveAuthenticatedTechnicianUseCase {

    Technician resolve(AuthenticatedUser authenticatedUser);

    default Long resolveId(AuthenticatedUser authenticatedUser) {
        return resolve(authenticatedUser).id();
    }
}
