package com.mechsync.modules.servicereports.application.port.in;

import com.mechsync.modules.servicereports.application.dto.CreateServiceReportCommand;
import com.mechsync.modules.servicereports.domain.model.ServiceReport;

public interface CreateServiceReportUseCase {
    ServiceReport create(CreateServiceReportCommand command);
}
