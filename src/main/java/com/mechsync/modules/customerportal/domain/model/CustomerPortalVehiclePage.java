package com.mechsync.modules.customerportal.domain.model;

import java.util.List;

public record CustomerPortalVehiclePage(
        List<CustomerPortalVehicleSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public CustomerPortalVehiclePage {
        content = List.copyOf(content);
    }
}
