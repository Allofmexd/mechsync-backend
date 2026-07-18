package com.mechsync.modules.workorders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkOrderRevisionServiceLine(
        Long id,
        Long revisionId,
        Integer lineNumber,
        Long serviceId,
        String nameSnapshot,
        String descriptionSnapshot,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        String notes,
        LocalDateTime createdAt) {
}
