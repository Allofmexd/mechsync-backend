package com.mechsync.modules.users.infrastructure.repository;

import com.mechsync.modules.users.infrastructure.persistence.RoleJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByName(String name);
}
