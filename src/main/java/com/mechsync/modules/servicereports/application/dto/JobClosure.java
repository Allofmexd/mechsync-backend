package com.mechsync.modules.servicereports.application.dto;

import java.math.BigDecimal;

public record JobClosure(
        Long jobId,
        String status,
        BigDecimal actualSubtotal,
        BigDecimal actualIva,
        BigDecimal actualTotal) {
}
