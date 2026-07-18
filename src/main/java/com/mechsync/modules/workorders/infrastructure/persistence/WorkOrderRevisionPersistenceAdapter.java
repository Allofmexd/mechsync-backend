package com.mechsync.modules.workorders.infrastructure.persistence;

import com.mechsync.modules.workorders.application.dto.WorkOrderRevisionPage;
import com.mechsync.modules.workorders.application.port.out.*;
import com.mechsync.modules.workorders.domain.exception.*;
import com.mechsync.modules.workorders.domain.model.*;
import com.mechsync.modules.workorders.infrastructure.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderRevisionPersistenceAdapter implements WorkOrderRevisionRepositoryPort {

    private final WorkOrderJpaRepository workOrders;
    private final WorkOrderRevisionJpaRepository revisions;
    private final WorkOrderRevisionServiceJpaRepository services;
    private final WorkOrderRevisionPartJpaRepository parts;
    private final WorkOrderRevisionStatusJpaRepository statuses;
    private final WorkOrderAcceptanceMethodJpaRepository methods;

    public WorkOrderRevisionPersistenceAdapter(
            WorkOrderJpaRepository workOrders,
            WorkOrderRevisionJpaRepository revisions,
            WorkOrderRevisionServiceJpaRepository services,
            WorkOrderRevisionPartJpaRepository parts,
            WorkOrderRevisionStatusJpaRepository statuses,
            WorkOrderAcceptanceMethodJpaRepository methods) {
        this.workOrders = workOrders;
        this.revisions = revisions;
        this.services = services;
        this.parts = parts;
        this.statuses = statuses;
        this.methods = methods;
    }

    @Override
    public boolean workOrderExists(Long workOrderId) {
        return workOrders.existsById(workOrderId);
    }

    @Override
    public Optional<WorkOrderRevisionParent> lockWorkOrder(Long workOrderId) {
        return workOrders.findByIdForUpdate(workOrderId).map(this::toParent);
    }

    @Override
    public boolean isAssignedToTechnicianUser(Long workOrderId, Long userId) {
        return revisions.countAssignedToTechnicianUser(workOrderId, userId) > 0;
    }

    @Override
    public WorkOrderRevisionPage findAll(Long workOrderId, int page, int size) {
        Page<WorkOrderRevisionJpaEntity> result = revisions.findByWorkOrderId(
                workOrderId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "revisionNumber")));
        WorkOrderJpaEntity parent = workOrders.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        Map<Long, WorkOrderRevisionStatus> statusMap = statusMap();
        Map<Long, String> methodMap = methodMap();
        return new WorkOrderRevisionPage(
                result.getContent().stream()
                        .map(entity -> toDomain(entity, false, parent, statusMap, methodMap))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<WorkOrderRevision> findById(
            Long workOrderId, Long revisionId, boolean withLines) {
        Optional<WorkOrderRevisionJpaEntity> entity = revisions.findByIdAndWorkOrderId(
                revisionId, workOrderId);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        WorkOrderJpaEntity parent = workOrders.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        return Optional.of(toDomain(entity.get(), withLines, parent, statusMap(), methodMap()));
    }

    @Override
    public Optional<WorkOrderRevision> findCurrent(Long workOrderId, boolean withLines) {
        return findByPointer(workOrderId, true, withLines);
    }

    @Override
    public Optional<WorkOrderRevision> findFinalApproved(Long workOrderId, boolean withLines) {
        return findByPointer(workOrderId, false, withLines);
    }

    private Optional<WorkOrderRevision> findByPointer(
            Long workOrderId, boolean current, boolean withLines) {
        Optional<WorkOrderJpaEntity> parent = workOrders.findById(workOrderId);
        if (parent.isEmpty()) {
            return Optional.empty();
        }
        Long revisionId = current
                ? parent.get().getCurrentRevisionId()
                : parent.get().getFinalApprovedRevisionId();
        if (revisionId == null) {
            return Optional.empty();
        }
        return revisions.findByIdAndWorkOrderId(revisionId, workOrderId)
                .map(entity -> toDomain(entity, withLines, parent.get(), statusMap(), methodMap()));
    }

    @Override
    public int nextRevisionNumber(Long workOrderId) {
        return revisions.nextRevisionNumber(workOrderId);
    }

    @Override
    public boolean technicianExists(Long technicianId) {
        return technicianId != null && revisions.countTechniciansById(technicianId) > 0;
    }

    @Override
    public boolean userExists(Long userId) {
        return revisions.countUsersById(userId) > 0;
    }

    @Override
    public Optional<CatalogSnapshot> findServiceSnapshot(Long serviceId) {
        return revisions.findServiceSnapshot(serviceId).map(this::toCatalogSnapshot);
    }

    @Override
    public Optional<CatalogSnapshot> findPartSnapshot(Long partId) {
        return revisions.findPartSnapshot(partId).map(this::toCatalogSnapshot);
    }

    @Override
    public boolean acceptanceMethodExists(String code) {
        return methods.findByCode(code).isPresent();
    }

    @Override
    public WorkOrderRevision insert(WorkOrderRevision revision) {
        Long draftStatusId = statusId(WorkOrderRevisionStatus.DRAFT);
        WorkOrderRevisionJpaEntity saved = revisions.saveAndFlush(new WorkOrderRevisionJpaEntity(
                revision.workOrderId(),
                revision.revisionNumber(),
                draftStatusId,
                revision.technicianId(),
                revision.estimatedStartDate(),
                revision.estimatedDeliveryDate(),
                revision.estimatedHours(),
                revision.subtotalAmount(),
                revision.applyIva(),
                revision.ivaRate(),
                revision.ivaAmount(),
                revision.totalAmount(),
                revision.currency(),
                revision.taxTreatmentNotes(),
                revision.technicalObservations(),
                revision.customerNotes(),
                revision.changeReason(),
                revision.createdByUserId()));
        List<WorkOrderRevisionServiceJpaEntity> savedServices = services.saveAllAndFlush(
                revision.services().stream()
                        .map(line -> new WorkOrderRevisionServiceJpaEntity(
                                saved.getId(), line.lineNumber(), line.serviceId(), line.nameSnapshot(),
                                line.descriptionSnapshot(), line.quantity(), line.unitPrice(),
                                line.lineSubtotal(), line.notes()))
                        .toList());
        List<WorkOrderRevisionPartJpaEntity> savedParts = parts.saveAllAndFlush(
                revision.parts().stream()
                        .map(line -> new WorkOrderRevisionPartJpaEntity(
                                saved.getId(), line.lineNumber(), line.partId(), line.nameSnapshot(),
                                line.partNumberSnapshot(), line.descriptionSnapshot(), line.quantity(),
                                line.unitPrice(), line.lineSubtotal(), line.notes()))
                        .toList());
        WorkOrderJpaEntity parent = workOrders.findById(revision.workOrderId())
                .orElseThrow(() -> new WorkOrderNotFoundException(revision.workOrderId()));
        return toDomain(saved, savedServices, savedParts, parent, statusMap(), methodMap());
    }

    @Override
    public void setCurrentRevision(Long workOrderId, Long revisionId) {
        WorkOrderJpaEntity parent = workOrders.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        parent.setCurrentRevisionId(revisionId);
        workOrders.saveAndFlush(parent);
    }

    @Override
    public void setFinalApprovedRevision(Long workOrderId, Long revisionId) {
        WorkOrderJpaEntity parent = workOrders.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        parent.setFinalApprovedRevisionId(revisionId);
        workOrders.saveAndFlush(parent);
    }

    @Override
    public void transition(
            Long workOrderId,
            Long revisionId,
            WorkOrderRevisionStatus target,
            LocalDateTime changedAt) {
        WorkOrderRevisionJpaEntity entity = revisions.findByIdAndWorkOrderId(revisionId, workOrderId)
                .orElseThrow(() -> new WorkOrderRevisionNotFoundException(workOrderId, revisionId));
        entity.transitionTo(statusId(target), changedAt);
        revisions.saveAndFlush(entity);
    }

    @Override
    public void approve(
            Long workOrderId,
            Long revisionId,
            Long approvedByUserId,
            LocalDateTime approvedAt,
            String acceptedByName,
            Long acceptedByUserId,
            LocalDateTime acceptedAt,
            String acceptanceMethod,
            String acceptanceNotes) {
        WorkOrderRevisionJpaEntity entity = revisions.findByIdAndWorkOrderId(revisionId, workOrderId)
                .orElseThrow(() -> new WorkOrderRevisionNotFoundException(workOrderId, revisionId));
        Long methodId = methods.findByCode(acceptanceMethod)
                .orElseThrow(() -> new InvalidWorkOrderRevisionException("Invalid acceptanceMethod"))
                .getId();
        entity.approve(
                statusId(WorkOrderRevisionStatus.APPROVED),
                approvedByUserId,
                approvedAt,
                acceptedByName,
                acceptedByUserId,
                acceptedAt,
                methodId,
                acceptanceNotes);
        revisions.saveAndFlush(entity);
    }

    private Long statusId(WorkOrderRevisionStatus status) {
        return statuses.findByCode(status.name())
                .orElseThrow(() -> new InvalidWorkOrderRevisionException(
                        "Revision status catalog is missing: " + status))
                .getId();
    }

    private Map<Long, WorkOrderRevisionStatus> statusMap() {
        return statuses.findAll().stream().collect(Collectors.toMap(
                WorkOrderRevisionStatusJpaEntity::getId,
                value -> WorkOrderRevisionStatus.valueOf(value.getCode())));
    }

    private Map<Long, String> methodMap() {
        return methods.findAll().stream().collect(Collectors.toMap(
                WorkOrderAcceptanceMethodJpaEntity::getId,
                WorkOrderAcceptanceMethodJpaEntity::getCode));
    }

    private WorkOrderRevision toDomain(
            WorkOrderRevisionJpaEntity entity,
            boolean withLines,
            WorkOrderJpaEntity parent,
            Map<Long, WorkOrderRevisionStatus> statusMap,
            Map<Long, String> methodMap) {
        List<WorkOrderRevisionServiceJpaEntity> serviceLines = withLines
                ? services.findByRevisionIdOrderByLineNumber(entity.getId())
                : List.of();
        List<WorkOrderRevisionPartJpaEntity> partLines = withLines
                ? parts.findByRevisionIdOrderByLineNumber(entity.getId())
                : List.of();
        return toDomain(entity, serviceLines, partLines, parent, statusMap, methodMap);
    }

    private WorkOrderRevision toDomain(
            WorkOrderRevisionJpaEntity entity,
            List<WorkOrderRevisionServiceJpaEntity> serviceLines,
            List<WorkOrderRevisionPartJpaEntity> partLines,
            WorkOrderJpaEntity parent,
            Map<Long, WorkOrderRevisionStatus> statusMap,
            Map<Long, String> methodMap) {
        WorkOrderRevisionStatus status = Optional.ofNullable(statusMap.get(entity.getRevisionStatusId()))
                .orElseThrow(() -> new InvalidWorkOrderRevisionException("Unknown revision status"));
        return new WorkOrderRevision(
                entity.getId(),
                entity.getWorkOrderId(),
                entity.getRevisionNumber(),
                status,
                entity.getTechnicianId(),
                entity.getEstimatedStartDate(),
                entity.getEstimatedDeliveryDate(),
                entity.getEstimatedHours(),
                entity.getSubtotalAmount(),
                entity.isApplyIva(),
                entity.getIvaRate(),
                entity.getIvaAmount(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getTaxTreatmentNotes(),
                entity.getTechnicalNotes(),
                entity.getCustomerNotes(),
                entity.getChangeReason(),
                entity.getCreatedByUserId(),
                entity.getApprovedByUserId(),
                entity.getApprovedAt(),
                entity.getAcceptedByName(),
                entity.getAcceptedByUserId(),
                entity.getAcceptedAt(),
                entity.getAcceptanceMethodId() == null
                        ? null
                        : methodMap.get(entity.getAcceptanceMethodId()),
                entity.getAcceptanceNotes(),
                entity.getLockVersion(),
                entity.getCreatedAt(),
                entity.getWorkflowUpdatedAt(),
                Objects.equals(parent.getCurrentRevisionId(), entity.getId()),
                Objects.equals(parent.getFinalApprovedRevisionId(), entity.getId()),
                serviceLines.stream().map(this::toDomain).toList(),
                partLines.stream().map(this::toDomain).toList());
    }

    private WorkOrderRevisionServiceLine toDomain(WorkOrderRevisionServiceJpaEntity entity) {
        return new WorkOrderRevisionServiceLine(
                entity.getId(), entity.getRevisionId(), entity.getLineNumber(), entity.getServiceId(),
                entity.getNameSnapshot(), entity.getDescriptionSnapshot(), entity.getQuantity(),
                entity.getUnitPrice(), entity.getLineSubtotal(), entity.getNotes(), entity.getCreatedAt());
    }

    private WorkOrderRevisionPartLine toDomain(WorkOrderRevisionPartJpaEntity entity) {
        return new WorkOrderRevisionPartLine(
                entity.getId(), entity.getRevisionId(), entity.getLineNumber(), entity.getPartId(),
                entity.getNameSnapshot(), entity.getPartNumberSnapshot(), entity.getDescriptionSnapshot(),
                entity.getQuantity(), entity.getUnitPrice(), entity.getLineSubtotal(), entity.getNotes(),
                entity.getCreatedAt());
    }

    private WorkOrderRevisionParent toParent(WorkOrderJpaEntity entity) {
        return new WorkOrderRevisionParent(
                entity.getId(),
                entity.getCurrentRevisionId(),
                entity.getFinalApprovedRevisionId(),
                entity.getLockVersion());
    }

    private CatalogSnapshot toCatalogSnapshot(CatalogSnapshotProjection projection) {
        return new CatalogSnapshot(
                projection.getName(), projection.getDescription(), projection.getReference());
    }
}
