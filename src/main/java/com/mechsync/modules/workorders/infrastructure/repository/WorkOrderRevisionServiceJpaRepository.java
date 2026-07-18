package com.mechsync.modules.workorders.infrastructure.repository;

import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderRevisionServiceJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRevisionServiceJpaRepository
        extends JpaRepository<WorkOrderRevisionServiceJpaEntity, Long> {
    List<WorkOrderRevisionServiceJpaEntity> findByRevisionIdOrderByLineNumber(Long revisionId);
}
