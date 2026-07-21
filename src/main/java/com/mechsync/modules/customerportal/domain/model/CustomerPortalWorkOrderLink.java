package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;

public record CustomerPortalWorkOrderLink(
        Long workOrderId,
        LocalDateTime workOrderDate,
        String visibleStatus,
        boolean quotationAvailable,
        Long jobId) {
}
