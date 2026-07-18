package com.mechsync.modules.workorders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkOrderRevisionPartLine(
        Long id,
        Long revisionId,
        Integer lineNumber,
        Long partId,
        String nameSnapshot,
        String partNumberSnapshot,
        String descriptionSnapshot,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        String notes,
        LocalDateTime createdAt) {
}
