package com.mechsync.modules.servicereports.application.port.in;

import com.mechsync.modules.servicereports.application.dto.GeneratedServiceReportPdf;

public interface GenerateServiceReportPdfUseCase {
    GeneratedServiceReportPdf generate(Long reportId);
    GeneratedServiceReportPdf generateAssignedTo(Long reportId, Long technicianId);
}
