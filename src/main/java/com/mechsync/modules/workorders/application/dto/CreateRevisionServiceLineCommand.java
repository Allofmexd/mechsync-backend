package com.mechsync.modules.workorders.application.dto;

import java.math.BigDecimal;

public record CreateRevisionServiceLineCommand(
        Integer lineNumber,
        Long serviceId,
        String nameSnapshot,
        String descriptionSnapshot,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        String notes) {
}
