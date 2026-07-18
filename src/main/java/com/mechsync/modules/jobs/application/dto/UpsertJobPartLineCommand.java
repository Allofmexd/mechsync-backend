package com.mechsync.modules.jobs.application.dto;

import java.math.BigDecimal;

public record UpsertJobPartLineCommand(
        Long jobId,
        Long lineId,
        Long partId,
        BigDecimal quantity,
        BigDecimal unitPrice) {
}
