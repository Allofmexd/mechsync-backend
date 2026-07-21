package com.mechsync.modules.customerportal.domain.model;

import java.math.BigDecimal;

public record CustomerPortalQuotationServiceLine(
        String serviceName,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
