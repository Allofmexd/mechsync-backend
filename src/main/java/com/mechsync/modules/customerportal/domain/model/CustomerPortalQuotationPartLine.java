package com.mechsync.modules.customerportal.domain.model;

import java.math.BigDecimal;

public record CustomerPortalQuotationPartLine(
        String partName,
        String partNumber,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
