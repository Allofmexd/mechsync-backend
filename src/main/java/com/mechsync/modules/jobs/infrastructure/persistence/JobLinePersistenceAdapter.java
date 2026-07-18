package com.mechsync.modules.jobs.infrastructure.persistence;

import com.mechsync.modules.jobs.application.port.out.JobLineRepositoryPort;
import com.mechsync.modules.jobs.domain.exception.JobConflictException;
import com.mechsync.modules.jobs.domain.exception.JobLineNotFoundException;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import com.mechsync.modules.jobs.infrastructure.repository.JobPartLineJpaRepository;
import com.mechsync.modules.jobs.infrastructure.repository.JobPartLineView;
import com.mechsync.modules.jobs.infrastructure.repository.JobServiceLineJpaRepository;
import com.mechsync.modules.jobs.infrastructure.repository.JobServiceLineView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class JobLinePersistenceAdapter implements JobLineRepositoryPort {
    private final JobServiceLineJpaRepository services;
    private final JobPartLineJpaRepository parts;

    public JobLinePersistenceAdapter(JobServiceLineJpaRepository services,
            JobPartLineJpaRepository parts) {
        this.services = services;
        this.parts = parts;
    }

    @Override
    public Optional<String> findServiceName(Long serviceId) {
        return serviceId == null ? Optional.empty() : services.findServiceName(serviceId);
    }

    @Override
    public Optional<String> findPartName(Long partId) {
        return partId == null ? Optional.empty() : parts.findPartName(partId);
    }

    @Override
    public List<JobServiceLine> findServicesByJobId(Long jobId) {
        return services.findViewsByJobId(jobId).stream().map(this::toService).toList();
    }

    @Override
    public Optional<JobServiceLine> findServiceByIdAndJobIdForUpdate(Long lineId, Long jobId) {
        return services.findByIdAndJobIdForUpdate(lineId, jobId).map(entity -> toService(entity,
                services.findServiceName(entity.getServiceId()).orElse("Unknown service")));
    }

    @Override
    public boolean serviceAlreadyUsed(Long jobId, Long serviceId, Long excludedLineId) {
        return excludedLineId == null
                ? services.existsByJobIdAndServiceId(jobId, serviceId)
                : services.existsByJobIdAndServiceIdAndIdNot(jobId, serviceId, excludedLineId);
    }

    @Override
    public JobServiceLine saveService(JobServiceLine line) {
        JobServiceLineJpaEntity entity;
        if (line.id() == null) {
            entity = new JobServiceLineJpaEntity(line.jobId(), line.serviceId(), line.quantity(),
                    line.unitPrice(), line.lineSubtotal());
        } else {
            entity = services.findByIdAndJobIdForUpdate(line.id(), line.jobId())
                    .orElseThrow(() -> new JobLineNotFoundException(
                            "Service", line.id(), line.jobId()));
            entity.update(line.serviceId(), line.quantity(), line.unitPrice(), line.lineSubtotal(),
                    line.updatedAt() == null ? LocalDateTime.now() : line.updatedAt());
        }
        try {
            return toService(services.saveAndFlush(entity), line.serviceName());
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job service line violates database integrity");
        }
    }

    @Override
    public void deleteService(Long lineId, Long jobId) {
        try {
            JobServiceLineJpaEntity entity = services.findByIdAndJobIdForUpdate(lineId, jobId)
                    .orElseThrow(() -> new JobLineNotFoundException("Service", lineId, jobId));
            services.delete(entity);
            services.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job service line cannot be deleted");
        }
    }

    @Override
    public List<JobPartLine> findPartsByJobId(Long jobId) {
        return parts.findViewsByJobId(jobId).stream().map(this::toPart).toList();
    }

    @Override
    public Optional<JobPartLine> findPartByIdAndJobIdForUpdate(Long lineId, Long jobId) {
        return parts.findByIdAndJobIdForUpdate(lineId, jobId).map(entity -> toPart(entity,
                parts.findPartName(entity.getPartId()).orElse("Unknown part")));
    }

    @Override
    public boolean partAlreadyUsed(Long jobId, Long partId, Long excludedLineId) {
        return excludedLineId == null
                ? parts.existsByJobIdAndPartId(jobId, partId)
                : parts.existsByJobIdAndPartIdAndIdNot(jobId, partId, excludedLineId);
    }

    @Override
    public JobPartLine savePart(JobPartLine line) {
        JobPartLineJpaEntity entity;
        if (line.id() == null) {
            entity = new JobPartLineJpaEntity(line.jobId(), line.partId(), line.quantity(),
                    line.unitPrice(), line.lineSubtotal());
        } else {
            entity = parts.findByIdAndJobIdForUpdate(line.id(), line.jobId())
                    .orElseThrow(() -> new JobLineNotFoundException(
                            "Part", line.id(), line.jobId()));
            entity.update(line.partId(), line.quantity(), line.unitPrice(), line.lineSubtotal(),
                    line.updatedAt() == null ? LocalDateTime.now() : line.updatedAt());
        }
        try {
            return toPart(parts.saveAndFlush(entity), line.partName());
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job part line violates database integrity");
        }
    }

    @Override
    public void deletePart(Long lineId, Long jobId) {
        try {
            JobPartLineJpaEntity entity = parts.findByIdAndJobIdForUpdate(lineId, jobId)
                    .orElseThrow(() -> new JobLineNotFoundException("Part", lineId, jobId));
            parts.delete(entity);
            parts.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new JobConflictException("The Job part line cannot be deleted");
        }
    }

    @Override
    public BigDecimal calculateActualSubtotal(Long jobId) {
        BigDecimal serviceSubtotal = Optional.ofNullable(services.sumSubtotalByJobId(jobId))
                .orElse(BigDecimal.ZERO);
        BigDecimal partSubtotal = Optional.ofNullable(parts.sumSubtotalByJobId(jobId))
                .orElse(BigDecimal.ZERO);
        return serviceSubtotal.add(partSubtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private JobServiceLine toService(JobServiceLineView view) {
        return new JobServiceLine(view.getLineId(), view.getJobId(), view.getCatalogId(),
                view.getCatalogName(), view.getQuantity(), view.getUnitPrice(),
                view.getLineSubtotal(), view.getCreatedAt(), view.getUpdatedAt());
    }

    private JobServiceLine toService(JobServiceLineJpaEntity entity, String name) {
        return new JobServiceLine(entity.getId(), entity.getJobId(), entity.getServiceId(), name,
                entity.getQuantity(), entity.getActualUnitPrice(), entity.getActualSubtotal(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private JobPartLine toPart(JobPartLineView view) {
        return new JobPartLine(view.getLineId(), view.getJobId(), view.getCatalogId(),
                view.getCatalogName(), view.getQuantity(), view.getUnitPrice(),
                view.getLineSubtotal(), view.getCreatedAt(), view.getUpdatedAt());
    }

    private JobPartLine toPart(JobPartLineJpaEntity entity, String name) {
        return new JobPartLine(entity.getId(), entity.getJobId(), entity.getPartId(), name,
                entity.getQuantityUsed(), entity.getActualUnitPrice(), entity.getActualSubtotal(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
