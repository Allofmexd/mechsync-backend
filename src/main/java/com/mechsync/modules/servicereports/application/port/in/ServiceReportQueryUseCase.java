package com.mechsync.modules.servicereports.application.port.in;

import com.mechsync.modules.servicereports.application.dto.ServiceReportPage;
import com.mechsync.modules.servicereports.domain.model.ServiceReport;

public interface ServiceReportQueryUseCase {
    ServiceReportPage list(int page, int size);
    ServiceReportPage listAssignedTo(Long technicianId, int page, int size);
    ServiceReport get(Long reportId);
    ServiceReport getAssignedTo(Long reportId, Long technicianId);
    ServiceReport getByJobId(Long jobId);
    ServiceReport getByJobIdAssignedTo(Long jobId, Long technicianId);
}
