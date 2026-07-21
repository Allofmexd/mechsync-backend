package com.mechsync.modules.specialties.infrastructure.repository;

import com.mechsync.modules.specialties.infrastructure.persistence.SpecialtyJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyJpaRepository extends JpaRepository<SpecialtyJpaEntity, Long> {
    List<SpecialtyJpaEntity> findAllByOrderByNameAsc();
}
