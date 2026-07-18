package com.mechsync.modules.jobs.application.dto;

import java.math.BigDecimal;

public record UpsertJobServiceLineCommand(
        Long jobId,
        Long lineId,
        Long serviceId,
        BigDecimal quantity,
        BigDecimal unitPrice) {
}
