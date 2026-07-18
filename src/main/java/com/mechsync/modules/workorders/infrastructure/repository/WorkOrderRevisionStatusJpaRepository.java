package com.mechsync.modules.workorders.infrastructure.repository;

import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderRevisionStatusJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRevisionStatusJpaRepository
        extends JpaRepository<WorkOrderRevisionStatusJpaEntity, Long> {
    Optional<WorkOrderRevisionStatusJpaEntity> findByCode(String code);
}
