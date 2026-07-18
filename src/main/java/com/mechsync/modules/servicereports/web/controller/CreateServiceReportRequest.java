package com.mechsync.modules.servicereports.web.controller;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateServiceReportRequest(
        @NotNull @Positive Long jobId,
        @NotBlank String finalDescription,
        boolean customerConfirmation,
        LocalDateTime deliveredAt) {
}
