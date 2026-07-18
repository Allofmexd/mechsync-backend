package com.mechsync.modules.workorders.application.port.out;

import com.mechsync.modules.workorders.application.dto.WorkOrderRevisionPage;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevisionStatus;
import java.time.LocalDateTime;
import java.util.Optional;

public interface WorkOrderRevisionRepositoryPort {
    boolean workOrderExists(Long workOrderId);

    Optional<WorkOrderRevisionParent> lockWorkOrder(Long workOrderId);

    boolean isAssignedToTechnicianUser(Long workOrderId, Long userId);

    WorkOrderRevisionPage findAll(Long workOrderId, int page, int size);

    Optional<WorkOrderRevision> findById(Long workOrderId, Long revisionId, boolean withLines);

    Optional<WorkOrderRevision> findCurrent(Long workOrderId, boolean withLines);

    Optional<WorkOrderRevision> findFinalApproved(Long workOrderId, boolean withLines);

    int nextRevisionNumber(Long workOrderId);

    boolean technicianExists(Long technicianId);

    boolean userExists(Long userId);

    Optional<CatalogSnapshot> findServiceSnapshot(Long serviceId);

    Optional<CatalogSnapshot> findPartSnapshot(Long partId);

    boolean acceptanceMethodExists(String code);

    WorkOrderRevision insert(WorkOrderRevision revision);

    void setCurrentRevision(Long workOrderId, Long revisionId);

    void setFinalApprovedRevision(Long workOrderId, Long revisionId);

    void transition(
            Long workOrderId,
            Long revisionId,
            WorkOrderRevisionStatus target,
            LocalDateTime changedAt);

    void approve(
            Long workOrderId,
            Long revisionId,
            Long approvedByUserId,
            LocalDateTime approvedAt,
            String acceptedByName,
            Long acceptedByUserId,
            LocalDateTime acceptedAt,
            String acceptanceMethod,
            String acceptanceNotes);
}
