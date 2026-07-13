package com.mechsync.modules.vehicles.application.dto;

import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.util.List;

public record VehiclePage(
        List<Vehicle> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public VehiclePage {
        content = List.copyOf(content);
    }
}
