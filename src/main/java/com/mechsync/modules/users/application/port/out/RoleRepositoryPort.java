package com.mechsync.modules.users.application.port.out;

import com.mechsync.modules.users.domain.model.Role;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
}
