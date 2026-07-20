package com.mechsync.modules.servicereports.application.port.out;

import com.mechsync.modules.servicereports.domain.model.ServiceReportPdfData;

public interface ServiceReportPdfGeneratorPort {
    byte[] generate(ServiceReportPdfData data);
}
