package com.mechsync.modules.servicereports.application.port.out;

import com.mechsync.modules.servicereports.application.dto.JobClosure;
import com.mechsync.modules.servicereports.application.dto.ServiceReportPage;
import com.mechsync.modules.servicereports.domain.model.ServiceReport;
import com.mechsync.modules.servicereports.domain.model.ServiceReportStatus;
import java.util.Optional;

public interface ServiceReportRepositoryPort {
    Optional<JobClosure> findJobClosure(Long jobId);
    boolean existsByJobId(Long jobId);
    Long requireStatusId(ServiceReportStatus status);
    ServiceReport insert(ServiceReport report, Long statusId);
    ServiceReportPage findAll(int page, int size);
    Optional<ServiceReport> findById(Long reportId);
    Optional<ServiceReport> findByJobId(Long jobId);
}
