package com.mechsync.modules.workorders.domain.model;

import java.math.BigDecimal;

public record QuotationAmounts(
        BigDecimal subtotalAmount,
        boolean applyIva,
        BigDecimal ivaRate,
        BigDecimal ivaAmount,
        BigDecimal totalAmount) {
}
