package com.mechsync.modules.servicereports.application.dto;

import java.time.LocalDateTime;

public record CreateServiceReportCommand(
        Long jobId,
        String finalDescription,
        boolean customerConfirmation,
        LocalDateTime deliveredAt) {
}
