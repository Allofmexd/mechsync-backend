package com.mechsync.modules.servicereports.application.port.out;

import com.mechsync.modules.servicereports.domain.model.ServiceReportPdfData;
import java.util.Optional;

public interface ServiceReportPdfDataPort {
    Optional<ServiceReportPdfData> findPdfDataByReportId(Long reportId);
    Optional<ServiceReportPdfData> findPdfDataByReportIdAndTechnicianId(
            Long reportId, Long technicianId);
}
