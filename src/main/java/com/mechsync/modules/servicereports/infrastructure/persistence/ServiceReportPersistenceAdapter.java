package com.mechsync.modules.servicereports.infrastructure.persistence;

import com.mechsync.modules.catalogs.infrastructure.persistence.CatalogStatusJpaEntity;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import com.mechsync.modules.servicereports.application.dto.*;
import com.mechsync.modules.servicereports.application.port.out.*;
import com.mechsync.modules.servicereports.domain.exception.*;
import com.mechsync.modules.servicereports.domain.model.*;
import com.mechsync.modules.servicereports.infrastructure.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

@Component
public class ServiceReportPersistenceAdapter
        implements ServiceReportRepositoryPort, ServiceReportPdfDataPort {
    private static final String CONTEXT = "SERVICE_REPORTS";
    private final ServiceReportJpaRepository reports;
    private final CatalogStatusJpaRepository statuses;

    public ServiceReportPersistenceAdapter(ServiceReportJpaRepository reports,
            CatalogStatusJpaRepository statuses) {
        this.reports = reports;
        this.statuses = statuses;
    }

    @Override
    public Optional<JobClosure> findJobClosure(Long jobId) {
        return reports.findJobClosure(jobId).map(view -> new JobClosure(view.getJobId(),
                view.getStatusCode(), view.getActualSubtotal(), view.getActualIva(),
                view.getActualTotal()));
    }

    @Override
    public boolean existsByJobId(Long jobId) {
        return reports.existsByJobId(jobId);
    }

    @Override
    public Long requireStatusId(ServiceReportStatus status) {
        return statusEntities().stream().filter(value -> status.name().equals(value.getCode()))
                .findFirst().orElseThrow(() ->
                        new ServiceReportStatusNotFoundException(status.name())).getId();
    }

    @Override
    public ServiceReport insert(ServiceReport report, Long statusId) {
        ServiceReportJpaEntity entity = new ServiceReportJpaEntity(report.jobId(),
                report.reportDate(), report.finalDescription(), report.finalSubtotal(),
                report.finalIva(), report.finalTotal(), report.customerConfirmation(),
                report.deliveredAt(), statusId, report.createdAt());
        try {
            return toDomain(reports.saveAndFlush(entity), statusMap());
        } catch (DataIntegrityViolationException exception) {
            throw new ServiceReportConflictException(
                    "The Service Report violates Job uniqueness or referential integrity");
        }
    }

    @Override
    public ServiceReportPage findAll(int page, int size) {
        Page<ServiceReportJpaEntity> result = reports.findAll(PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "id")));
        Map<Long, ServiceReportStatus> statusMap = statusMap();
        return new ServiceReportPage(result.getContent().stream()
                .map(value -> toDomain(value, statusMap)).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Optional<ServiceReport> findById(Long reportId) {
        Map<Long, ServiceReportStatus> statusMap = statusMap();
        return reports.findById(reportId).map(value -> toDomain(value, statusMap));
    }

    @Override
    public Optional<ServiceReport> findByJobId(Long jobId) {
        Map<Long, ServiceReportStatus> statusMap = statusMap();
        return reports.findByJobId(jobId).map(value -> toDomain(value, statusMap));
    }

    @Override
    public Optional<ServiceReportPdfData> findPdfDataByReportId(Long reportId) {
        return reports.findPdfHeader(reportId).map(header -> new ServiceReportPdfData(
                header.getReportId(), header.getJobId(), parseStatus(header.getReportStatus()),
                header.getReportDate(), header.getFinalDescription(), header.getFinalSubtotal(),
                header.getFinalIva(), header.getFinalTotal(),
                Boolean.TRUE.equals(header.getCustomerConfirmation()), header.getDeliveredAt(),
                header.getWorkOrderId(), header.getVehicleIntakeId(), header.getTechnicianId(),
                header.getTechnicianName(), header.getCustomerId(), header.getCustomerName(),
                header.getVehicleId(), header.getVehicleBrand(), header.getVehicleModel(),
                header.getVehicleYear(), header.getLicensePlate(), header.getVin(),
                header.getMileage(),
                reports.findPdfServices(reportId).stream()
                        .map(line -> new ServiceReportPdfData.ServiceLine(line.getName(),
                                line.getQuantity(), line.getUnitPrice(), line.getSubtotal()))
                        .toList(),
                reports.findPdfParts(reportId).stream()
                        .map(line -> new ServiceReportPdfData.PartLine(line.getName(),
                                line.getQuantity(), line.getMeasurementUnit(), line.getUnitPrice(),
                                line.getSubtotal()))
                        .toList()));
    }

    private List<CatalogStatusJpaEntity> statusEntities() {
        return statuses.findAllByContextOrderByIdAsc(CONTEXT);
    }

    private Map<Long, ServiceReportStatus> statusMap() {
        return statusEntities().stream().collect(Collectors.toMap(CatalogStatusJpaEntity::getId,
                value -> {
                    try {
                        return ServiceReportStatus.valueOf(value.getCode());
                    } catch (IllegalArgumentException exception) {
                        throw new ServiceReportConflictException(
                                "Unsupported SERVICE_REPORTS status: " + value.getCode());
                    }
                }));
    }

    private ServiceReport toDomain(ServiceReportJpaEntity entity,
            Map<Long, ServiceReportStatus> statusMap) {
        ServiceReportStatus status = Optional.ofNullable(statusMap.get(entity.getStatusId()))
                .orElseThrow(() -> new ServiceReportConflictException(
                        "Unknown SERVICE_REPORTS status"));
        return new ServiceReport(entity.getId(), entity.getJobId(), status,
                entity.getReportDate(), entity.getFinalDescription(), entity.getFinalSubtotal(),
                entity.getFinalIva(), entity.getFinalTotal(), entity.isCustomerConfirmation(),
                entity.getDeliveredAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private ServiceReportStatus parseStatus(String code) {
        try {
            return ServiceReportStatus.valueOf(code);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ServiceReportConflictException(
                    "Unsupported SERVICE_REPORTS status: " + code);
        }
    }
}
