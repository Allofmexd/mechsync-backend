package com.mechsync.modules.jobs.application.usecase;

import com.mechsync.modules.jobs.application.dto.*;
import com.mechsync.modules.jobs.application.port.in.*;
import com.mechsync.modules.jobs.application.port.out.JobRepositoryPort;
import com.mechsync.modules.jobs.application.port.out.JobRevisionAuthorization;
import com.mechsync.modules.jobs.domain.exception.*;
import com.mechsync.modules.jobs.domain.model.Job;
import com.mechsync.modules.jobs.domain.model.JobStatus;
import com.mechsync.modules.jobs.domain.service.JobMoneyValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobService implements CreateJobUseCase, JobQueryUseCase, JobWorkflowUseCase {
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final JobRepositoryPort repository;
    private final JobMoneyValidator moneyValidator = new JobMoneyValidator();

    public JobService(JobRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Job create(CreateJobCommand command) {
        if (!repository.workOrderExists(command.workOrderId())) {
            throw new JobWorkOrderNotFoundException(command.workOrderId());
        }
        JobRevisionAuthorization revision = repository
                .findRevisionAuthorization(command.initialApprovedRevisionId())
                .orElseThrow(() -> new JobRevisionNotFoundException(
                        command.initialApprovedRevisionId()));
        if (!Objects.equals(revision.workOrderId(), command.workOrderId())) {
            throw new InvalidJobException("The approved revision does not belong to the Work Order");
        }
        if (!"APPROVED".equals(revision.statusCode())) {
            throw new InvalidJobException("The Work Order Revision must be APPROVED");
        }
        Long finalApproved = repository.finalApprovedRevisionId(command.workOrderId())
                .orElseThrow(() -> new InvalidJobException(
                        "The Work Order does not have a final approved revision"));
        if (!Objects.equals(finalApproved, command.initialApprovedRevisionId())) {
            throw new InvalidJobException(
                    "The revision must be the final approved revision of the Work Order");
        }
        if (!repository.technicianExists(command.technicianId())) {
            throw new JobTechnicianNotFoundException(command.technicianId());
        }
        if (repository.existsByWorkOrderId(command.workOrderId())
                || repository.existsByInitialApprovedRevisionId(command.initialApprovedRevisionId())) {
            throw new JobConflictException("A Job already exists for this authorized Work Order revision");
        }
        Long pendingStatusId = repository.requireStatusId(JobStatus.PENDIENTE);
        Job job = new Job(
                null, command.workOrderId(), command.initialApprovedRevisionId(),
                command.technicianId(), JobStatus.PENDIENTE, command.scheduledStartDate(),
                null, null, null, null, ZERO_MONEY, ZERO_MONEY, ZERO_MONEY,
                trim(command.notes()), null, null, null);
        return repository.insert(job, pendingStatusId);
    }

    @Override
    public JobPage list(int page, int size) {
        return repository.findAll(page, size);
    }

    @Override
    public Job get(Long id) {
        return repository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    @Override
    @Transactional
    public Job start(Long id) {
        Job job = lock(id);
        requireTransition(job, JobStatus.EN_PROCESO);
        LocalDateTime now = LocalDateTime.now();
        return repository.update(copy(job, JobStatus.EN_PROCESO, now, null, null,
                job.realSubtotalAmount(), job.realIvaAmount(), job.realTotalAmount(),
                job.notes(), null, now), repository.requireStatusId(JobStatus.EN_PROCESO));
    }

    @Override
    @Transactional
    public Job complete(CompleteJobCommand command) {
        Job job = lock(command.jobId());
        requireTransition(job, JobStatus.COMPLETADO);
        if (job.startDate() == null) {
            throw new JobConflictException("A Job must be started before it can be completed");
        }
        JobMoneyValidator.Amounts amounts = moneyValidator.validate(
                command.realSubtotalAmount(), command.realIvaAmount(), command.realTotalAmount());
        if (amounts.subtotal().compareTo(job.realSubtotalAmount()) != 0) {
            throw new InvalidJobException(
                    "The final subtotal must match the sum of actual Job service and part lines");
        }
        LocalDateTime now = LocalDateTime.now();
        String notes = trim(command.notes()) == null ? job.notes() : trim(command.notes());
        return repository.update(copy(job, JobStatus.COMPLETADO, job.startDate(), now, null,
                amounts.subtotal(), amounts.iva(), amounts.total(), notes, null, now),
                repository.requireStatusId(JobStatus.COMPLETADO));
    }

    @Override
    @Transactional
    public Job cancel(CancelJobCommand command) {
        Job job = lock(command.jobId());
        requireTransition(job, JobStatus.CANCELADO);
        LocalDateTime now = LocalDateTime.now();
        return repository.update(copy(job, JobStatus.CANCELADO, job.startDate(), null, now,
                job.realSubtotalAmount(), job.realIvaAmount(), job.realTotalAmount(), job.notes(),
                trim(command.cancellationNotes()), now),
                repository.requireStatusId(JobStatus.CANCELADO));
    }

    private Job lock(Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    private void requireTransition(Job job, JobStatus target) {
        if (!job.status().canTransitionTo(target)) {
            throw new JobConflictException(
                    "Invalid Job transition: " + job.status() + " -> " + target);
        }
    }

    private Job copy(Job job, JobStatus status, LocalDateTime startDate,
            LocalDateTime completionDate, LocalDateTime cancelledAt, BigDecimal subtotal,
            BigDecimal iva, BigDecimal total, String notes, String cancellationNotes,
            LocalDateTime updatedAt) {
        return new Job(job.id(), job.workOrderId(), job.initialApprovedRevisionId(),
                job.technicianId(), status, job.scheduledStartDate(), startDate, completionDate,
                cancelledAt, job.actualHours(), subtotal, iva, total, notes,
                cancellationNotes, job.createdAt(), updatedAt);
    }

    private String trim(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
