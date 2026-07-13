package com.mechsync.modules.users.infrastructure.repository;

import com.mechsync.modules.users.infrastructure.persistence.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserJpaEntity> findOneWithRolesById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
