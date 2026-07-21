package com.mechsync.modules.jobs.application.usecase;

import com.mechsync.modules.jobs.application.dto.UpsertJobPartLineCommand;
import com.mechsync.modules.jobs.application.dto.UpsertJobServiceLineCommand;
import com.mechsync.modules.jobs.application.port.in.JobLineUseCase;
import com.mechsync.modules.jobs.application.port.out.JobLineRepositoryPort;
import com.mechsync.modules.jobs.application.port.out.JobRepositoryPort;
import com.mechsync.modules.jobs.domain.exception.InvalidJobException;
import com.mechsync.modules.jobs.domain.exception.JobConflictException;
import com.mechsync.modules.jobs.domain.exception.JobLineCatalogNotFoundException;
import com.mechsync.modules.jobs.domain.exception.JobLineNotFoundException;
import com.mechsync.modules.jobs.domain.exception.JobNotFoundException;
import com.mechsync.modules.jobs.domain.model.Job;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import com.mechsync.modules.jobs.domain.model.JobStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobLineService implements JobLineUseCase {
    private static final int MONEY_SCALE = 2;
    private static final int MAX_INTEGER_DIGITS = 8;

    private final JobRepositoryPort jobs;
    private final JobLineRepositoryPort lines;

    public JobLineService(JobRepositoryPort jobs, JobLineRepositoryPort lines) {
        this.jobs = jobs;
        this.lines = lines;
    }

    @Override
    public List<JobServiceLine> listServices(Long jobId) {
        requireJob(jobId);
        return lines.findServicesByJobId(jobId);
    }

    @Override
    public List<JobServiceLine> listServicesAssignedTo(Long jobId, Long technicianId) {
        requireAssignedJob(jobId, technicianId);
        return lines.findServicesByJobId(jobId);
    }

    @Override
    @Transactional
    public JobServiceLine addService(UpsertJobServiceLineCommand command) {
        requireMutableJob(command.jobId());
        String name = lines.findServiceName(command.serviceId())
                .orElseThrow(() -> new JobLineCatalogNotFoundException(
                        "Service", command.serviceId()));
        if (lines.serviceAlreadyUsed(command.jobId(), command.serviceId(), null)) {
            throw new JobConflictException("The service is already registered for this Job");
        }
        Amounts amounts = calculate(command.quantity(), command.unitPrice());
        JobServiceLine saved = lines.saveService(new JobServiceLine(null, command.jobId(),
                command.serviceId(), name, amounts.quantity(), amounts.unitPrice(),
                amounts.subtotal(), null, null));
        synchronizeJobSubtotal(command.jobId());
        return saved;
    }

    @Override
    @Transactional
    public JobServiceLine updateService(UpsertJobServiceLineCommand command) {
        requireMutableJob(command.jobId());
        JobServiceLine current = lines.findServiceByIdAndJobIdForUpdate(
                command.lineId(), command.jobId())
                .orElseThrow(() -> new JobLineNotFoundException(
                        "Service", command.lineId(), command.jobId()));
        String name = lines.findServiceName(command.serviceId())
                .orElseThrow(() -> new JobLineCatalogNotFoundException(
                        "Service", command.serviceId()));
        if (lines.serviceAlreadyUsed(command.jobId(), command.serviceId(), command.lineId())) {
            throw new JobConflictException("The service is already registered for this Job");
        }
        Amounts amounts = calculate(command.quantity(), command.unitPrice());
        JobServiceLine saved = lines.saveService(new JobServiceLine(current.id(), current.jobId(),
                command.serviceId(), name, amounts.quantity(), amounts.unitPrice(),
                amounts.subtotal(), current.createdAt(), LocalDateTime.now()));
        synchronizeJobSubtotal(command.jobId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteService(Long jobId, Long lineId) {
        requireMutableJob(jobId);
        lines.findServiceByIdAndJobIdForUpdate(lineId, jobId)
                .orElseThrow(() -> new JobLineNotFoundException("Service", lineId, jobId));
        lines.deleteService(lineId, jobId);
        synchronizeJobSubtotal(jobId);
    }

    @Override
    public List<JobPartLine> listParts(Long jobId) {
        requireJob(jobId);
        return lines.findPartsByJobId(jobId);
    }

    @Override
    public List<JobPartLine> listPartsAssignedTo(Long jobId, Long technicianId) {
        requireAssignedJob(jobId, technicianId);
        return lines.findPartsByJobId(jobId);
    }

    @Override
    @Transactional
    public JobPartLine addPart(UpsertJobPartLineCommand command) {
        requireMutableJob(command.jobId());
        String name = lines.findPartName(command.partId())
                .orElseThrow(() -> new JobLineCatalogNotFoundException("Part", command.partId()));
        if (lines.partAlreadyUsed(command.jobId(), command.partId(), null)) {
            throw new JobConflictException("The part is already registered for this Job");
        }
        Amounts amounts = calculate(command.quantity(), command.unitPrice());
        JobPartLine saved = lines.savePart(new JobPartLine(null, command.jobId(), command.partId(),
                name, amounts.quantity(), amounts.unitPrice(), amounts.subtotal(), null, null));
        synchronizeJobSubtotal(command.jobId());
        return saved;
    }

    @Override
    @Transactional
    public JobPartLine updatePart(UpsertJobPartLineCommand command) {
        requireMutableJob(command.jobId());
        JobPartLine current = lines.findPartByIdAndJobIdForUpdate(command.lineId(), command.jobId())
                .orElseThrow(() -> new JobLineNotFoundException(
                        "Part", command.lineId(), command.jobId()));
        String name = lines.findPartName(command.partId())
                .orElseThrow(() -> new JobLineCatalogNotFoundException("Part", command.partId()));
        if (lines.partAlreadyUsed(command.jobId(), command.partId(), command.lineId())) {
            throw new JobConflictException("The part is already registered for this Job");
        }
        Amounts amounts = calculate(command.quantity(), command.unitPrice());
        JobPartLine saved = lines.savePart(new JobPartLine(current.id(), current.jobId(),
                command.partId(), name, amounts.quantity(), amounts.unitPrice(),
                amounts.subtotal(), current.createdAt(), LocalDateTime.now()));
        synchronizeJobSubtotal(command.jobId());
        return saved;
    }

    @Override
    @Transactional
    public void deletePart(Long jobId, Long lineId) {
        requireMutableJob(jobId);
        lines.findPartByIdAndJobIdForUpdate(lineId, jobId)
                .orElseThrow(() -> new JobLineNotFoundException("Part", lineId, jobId));
        lines.deletePart(lineId, jobId);
        synchronizeJobSubtotal(jobId);
    }

    private Job requireJob(Long jobId) {
        return jobs.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private Job requireAssignedJob(Long jobId, Long technicianId) {
        return jobs.findByIdAndTechnicianId(jobId, technicianId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private void requireMutableJob(Long jobId) {
        Job job = jobs.findByIdForUpdate(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        if (job.status() != JobStatus.PENDIENTE && job.status() != JobStatus.EN_PROCESO) {
            throw new JobConflictException("Job lines cannot change in status " + job.status());
        }
    }

    private Amounts calculate(BigDecimal quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidJobException("Line quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidJobException("Line unit price must be non-negative");
        }
        BigDecimal normalizedQuantity = normalize(quantity, "quantity");
        BigDecimal normalizedPrice = normalize(unitPrice, "unit price");
        BigDecimal subtotal = normalizedQuantity.multiply(normalizedPrice)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        validateIntegerDigits(subtotal, "line subtotal");
        return new Amounts(normalizedQuantity, normalizedPrice, subtotal);
    }

    private BigDecimal normalize(BigDecimal value, String field) {
        BigDecimal normalized;
        try {
            normalized = value.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidJobException("Line " + field + " supports at most 2 decimals");
        }
        validateIntegerDigits(normalized, field);
        return normalized;
    }

    private void validateIntegerDigits(BigDecimal value, String field) {
        int integerDigits = Math.max(0, value.precision() - value.scale());
        if (integerDigits > MAX_INTEGER_DIGITS) {
            throw new InvalidJobException("Line " + field + " exceeds DECIMAL(10,2)");
        }
    }

    private void synchronizeJobSubtotal(Long jobId) {
        jobs.updateActualSubtotal(jobId, lines.calculateActualSubtotal(jobId));
    }

    private record Amounts(BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
