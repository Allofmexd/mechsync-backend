package com.mechsync.modules.workorders.infrastructure.repository;

import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderRevisionPartJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRevisionPartJpaRepository
        extends JpaRepository<WorkOrderRevisionPartJpaEntity, Long> {
    List<WorkOrderRevisionPartJpaEntity> findByRevisionIdOrderByLineNumber(Long revisionId);
}
