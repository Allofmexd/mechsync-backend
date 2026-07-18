package com.mechsync.modules.jobs.application.dto;

import java.math.BigDecimal;

public record CompleteJobCommand(
        Long jobId,
        BigDecimal realSubtotalAmount,
        BigDecimal realIvaAmount,
        BigDecimal realTotalAmount,
        String notes) {
}
