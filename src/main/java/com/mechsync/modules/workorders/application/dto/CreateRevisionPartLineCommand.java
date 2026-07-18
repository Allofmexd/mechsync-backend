package com.mechsync.modules.workorders.application.dto;

import java.math.BigDecimal;

public record CreateRevisionPartLineCommand(
        Integer lineNumber,
        Long partId,
        String nameSnapshot,
        String descriptionSnapshot,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        String notes) {
}
