package com.mechsync.modules.jobs.infrastructure.persistence;

import com.mechsync.modules.catalogs.infrastructure.persistence.CatalogStatusJpaEntity;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import com.mechsync.modules.jobs.application.dto.JobPage;
import com.mechsync.modules.jobs.application.port.out.JobRepositoryPort;
import com.mechsync.modules.jobs.application.port.out.JobRevisionAuthorization;
import com.mechsync.modules.jobs.domain.exception.*;
import com.mechsync.modules.jobs.domain.model.Job;
import com.mechsync.modules.jobs.domain.model.JobStatus;
import com.mechsync.modules.jobs.infrastructure.repository.JobJpaRepository;
import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderRevisionJpaEntity;
import com.mechsync.modules.workorders.infrastructure.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JobPersistenceAdapter implements JobRepositoryPort {
    private static final String JOBS_CONTEXT = "JOBS";

    private final JobJpaRepository jobs;
    private final WorkOrderJpaRepository workOrders;
    private final WorkOrderRevisionJpaRepository revisions;
    private final WorkOrderRevisionStatusJpaRepository revisionStatuses;
    private final CatalogStatusJpaRepository statuses;

    public JobPersistenceAdapter(JobJpaRepository jobs, WorkOrderJpaRepository workOrders,
            WorkOrderRevisionJpaRepository revisions,
            WorkOrderRevisionStatusJpaRepository revisionStatuses,
            CatalogStatusJpaRepository statuses) {
        this.jobs = jobs;
        this.workOrders = workOrders;
        this.revisions = revisions;
        this.revisionStatuses = revisionStatuses;
        this.statuses = statuses;
    }

    @Override
    public boolean workOrderExists(Long workOrderId) {
        return workOrderId != null && workOrders.existsById(workOrderId);
    }

    @Override
    public Optional<Long> finalApprovedRevisionId(Long workOrderId) {
        return workOrders.findById(workOrderId).map(value -> value.getFinalApprovedRevisionId());
    }

    @Override
    public Optional<JobRevisionAuthorization> findRevisionAuthorization(Long revisionId) {
        Optional<WorkOrderRevisionJpaEntity> entity = revisions.findById(revisionId);
        if (entity.isEmpty()) return Optional.empty();
        String statusCode = revisionStatuses.findById(entity.get().getRevisionStatusId())
                .map(value -> value.getCode())
                .orElseThrow(() -> new InvalidJobException("Unknown Work Order Revision status"));
        return Optional.of(new JobRevisionAuthorization(entity.get().getWorkOrderId(), statusCode));
    }

    @Override
    public boolean technicianExists(Long technicianId) {
        return technicianId != null && jobs.countTechniciansById(technicianId) > 0;
    }

    @Override
    public boolean existsByWorkOrderId(Long workOrderId) {
        return jobs.existsByWorkOrderId(workOrderId);
    }

    @Override
    public boolean existsByInitialApprovedRevisionId(Long revisionId) {
        return jobs.existsByInitialApprovedRevisionId(revisionId);
    }

    @Override
    public Long requireStatusId(JobStatus status) {
        return statusEntities().stream()
                .filter(value -> status.name().equals(value.getCode()))
                .findFirst()
                .orElseThrow(() -> new JobStatusNotFoundException(status.name()))
                .getId();
    }

    @Override
    public Job insert(Job job, Long statusId) {
        try {
            JobJpaEntity entity = new JobJpaEntity(job.workOrderId(),
                    job.initialApprovedRevisionId(), job.technicianId(), job.scheduledStartDate(),
                    job.realSubtotalAmount(), job.realIvaAmount(), job.realTotalAmount(),
                    job.notes(), statusId);
            return toDomain(jobs.saveAndFlush(entity), statusMap());
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job violates the authorized workflow or already exists");
        }
    }

    @Override
    public JobPage findAll(int page, int size) {
        Page<JobJpaEntity> result = jobs.findAll(PageRequest.of(page, size));
        Map<Long, JobStatus> statusMap = statusMap();
        return new JobPage(result.getContent().stream()
                .map(value -> toDomain(value, statusMap)).toList(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Optional<Job> findById(Long id) {
        Map<Long, JobStatus> statusMap = statusMap();
        return jobs.findById(id).map(value -> toDomain(value, statusMap));
    }

    @Override
    public Optional<Job> findByIdForUpdate(Long id) {
        Map<Long, JobStatus> statusMap = statusMap();
        return jobs.findByIdForUpdate(id).map(value -> toDomain(value, statusMap));
    }

    @Override
    public Job update(Job job, Long statusId) {
        JobJpaEntity entity = jobs.findByIdForUpdate(job.id())
                .orElseThrow(() -> new JobNotFoundException(job.id()));
        entity.applyWorkflow(job.startDate(), job.completionDate(), job.cancelledAt(),
                job.realSubtotalAmount(), job.realIvaAmount(), job.realTotalAmount(), job.notes(),
                job.cancellationNotes(), statusId, job.updatedAt());
        try {
            return toDomain(jobs.saveAndFlush(entity), statusMap());
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job workflow update was rejected");
        }
    }

    private List<CatalogStatusJpaEntity> statusEntities() {
        return statuses.findAllByContextOrderByIdAsc(JOBS_CONTEXT);
    }

    private Map<Long, JobStatus> statusMap() {
        return statusEntities().stream().collect(Collectors.toMap(
                CatalogStatusJpaEntity::getId,
                value -> {
                    try {
                        return JobStatus.valueOf(value.getCode());
                    } catch (IllegalArgumentException exception) {
                        throw new InvalidJobException("Unsupported JOBS status: " + value.getCode());
                    }
                }));
    }

    private Job toDomain(JobJpaEntity entity, Map<Long, JobStatus> statusMap) {
        JobStatus status = Optional.ofNullable(statusMap.get(entity.getStatusId()))
                .orElseThrow(() -> new InvalidJobException("Unknown JOBS status"));
        return new Job(entity.getId(), entity.getWorkOrderId(),
                entity.getInitialApprovedRevisionId(), entity.getTechnicianId(), status,
                entity.getScheduledStartDate(), entity.getStartDate(), entity.getCompletionDate(),
                entity.getCancelledAt(), entity.getActualHours(), entity.getActualSubtotal(),
                entity.getActualIva(), entity.getActualTotal(), entity.getExecutionObservations(),
                entity.getCancellationNotes(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
