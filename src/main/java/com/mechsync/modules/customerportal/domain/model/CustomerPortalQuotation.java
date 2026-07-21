package com.mechsync.modules.customerportal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerPortalQuotation(
        Long workOrderId,
        Long revisionId,
        Integer revisionNumber,
        String visibleStatus,
        String currency,
        BigDecimal subtotal,
        boolean applyIva,
        BigDecimal ivaRate,
        BigDecimal ivaAmount,
        BigDecimal total,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        BigDecimal estimatedHours,
        String customerNotes,
        LocalDateTime acceptedAt,
        String acceptanceMethod,
        List<CustomerPortalQuotationServiceLine> services,
        List<CustomerPortalQuotationPartLine> parts) {

    public CustomerPortalQuotation {
        services = services == null ? List.of() : List.copyOf(services);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }
}
