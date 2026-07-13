package com.mechsync.modules.users.infrastructure.persistence;

import com.mechsync.modules.users.application.port.out.RoleRepositoryPort;
import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.infrastructure.repository.RoleJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository repository;

    public RolePersistenceAdapter(RoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return repository.findByName(name)
                .map(entity -> new Role(entity.getId(), entity.getName()));
    }
}
