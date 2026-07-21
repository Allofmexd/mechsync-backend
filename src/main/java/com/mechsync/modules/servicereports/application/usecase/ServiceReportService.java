package com.mechsync.modules.servicereports.application.usecase;

import com.mechsync.modules.servicereports.application.dto.*;
import com.mechsync.modules.servicereports.application.port.in.*;
import com.mechsync.modules.servicereports.application.port.out.ServiceReportRepositoryPort;
import com.mechsync.modules.servicereports.domain.exception.*;
import com.mechsync.modules.servicereports.domain.model.*;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServiceReportService implements CreateServiceReportUseCase, ServiceReportQueryUseCase {
    private final ServiceReportRepositoryPort repository;

    public ServiceReportService(ServiceReportRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ServiceReport create(CreateServiceReportCommand command) {
        JobClosure job = repository.findJobClosure(command.jobId())
                .orElseThrow(() -> new ServiceReportJobNotFoundException(command.jobId()));
        if (!"COMPLETADO".equals(job.status())) {
            throw new ServiceReportConflictException(
                    "A Service Report can only be created from a COMPLETADO Job");
        }
        if (repository.existsByJobId(command.jobId())) {
            throw new ServiceReportConflictException(
                    "A Service Report already exists for Job: " + command.jobId());
        }
        LocalDateTime now = LocalDateTime.now();
        ServiceReportStatus status = command.deliveredAt() == null
                ? ServiceReportStatus.COMPLETADO
                : ServiceReportStatus.ENTREGADO;
        ServiceReport report = new ServiceReport(null, job.jobId(), status, now,
                command.finalDescription().trim(), job.actualSubtotal(), job.actualIva(),
                job.actualTotal(), command.customerConfirmation(), command.deliveredAt(),
                now, null);
        return repository.insert(report, repository.requireStatusId(status));
    }

    @Override
    public ServiceReportPage list(int page, int size) {
        return repository.findAll(page, size);
    }

    @Override
    public ServiceReportPage listAssignedTo(Long technicianId, int page, int size) {
        return repository.findAllByTechnicianId(technicianId, page, size);
    }

    @Override
    public ServiceReport get(Long reportId) {
        return repository.findById(reportId)
                .orElseThrow(() -> new ServiceReportNotFoundException(reportId));
    }

    @Override
    public ServiceReport getAssignedTo(Long reportId, Long technicianId) {
        return repository.findByIdAndTechnicianId(reportId, technicianId)
                .orElseThrow(() -> new ServiceReportNotFoundException(reportId));
    }

    @Override
    public ServiceReport getByJobId(Long jobId) {
        return repository.findByJobId(jobId)
                .orElseThrow(() -> ServiceReportNotFoundException.forJob(jobId));
    }

    @Override
    public ServiceReport getByJobIdAssignedTo(Long jobId, Long technicianId) {
        return repository.findByJobIdAndTechnicianId(jobId, technicianId)
                .orElseThrow(() -> ServiceReportNotFoundException.forJob(jobId));
    }
}
