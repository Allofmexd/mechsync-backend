package com.mechsync.modules.workorders.infrastructure.repository;

import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderAcceptanceMethodJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderAcceptanceMethodJpaRepository
        extends JpaRepository<WorkOrderAcceptanceMethodJpaEntity, Long> {
    Optional<WorkOrderAcceptanceMethodJpaEntity> findByCode(String code);
}
