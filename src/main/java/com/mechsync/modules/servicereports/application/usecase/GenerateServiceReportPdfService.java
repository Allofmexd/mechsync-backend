package com.mechsync.modules.servicereports.application.usecase;

import com.mechsync.modules.servicereports.application.dto.GeneratedServiceReportPdf;
import com.mechsync.modules.servicereports.application.port.in.GenerateServiceReportPdfUseCase;
import com.mechsync.modules.servicereports.application.port.out.ServiceReportPdfDataPort;
import com.mechsync.modules.servicereports.application.port.out.ServiceReportPdfGeneratorPort;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportNotFoundException;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportPdfGenerationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GenerateServiceReportPdfService implements GenerateServiceReportPdfUseCase {
    private final ServiceReportPdfDataPort dataPort;
    private final ServiceReportPdfGeneratorPort generator;

    public GenerateServiceReportPdfService(ServiceReportPdfDataPort dataPort,
            ServiceReportPdfGeneratorPort generator) {
        this.dataPort = dataPort;
        this.generator = generator;
    }

    @Override
    public GeneratedServiceReportPdf generate(Long reportId) {
        var data = dataPort.findPdfDataByReportId(reportId)
                .orElseThrow(() -> new ServiceReportNotFoundException(reportId));
        byte[] content = generator.generate(data);
        if (content == null || content.length == 0) {
            throw new ServiceReportPdfGenerationException("The generated PDF is empty");
        }
        return new GeneratedServiceReportPdf("service-report-" + reportId + ".pdf", content);
    }
}
