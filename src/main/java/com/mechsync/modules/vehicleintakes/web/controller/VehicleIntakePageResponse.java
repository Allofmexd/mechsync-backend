package com.mechsync.modules.vehicleintakes.web.controller;

import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
import java.util.List;

public record VehicleIntakePageResponse(List<VehicleIntakeResponse> content, int page, int size,
        long totalElements, int totalPages) {
    public static VehicleIntakePageResponse from(VehicleIntakePage p) {
        return new VehicleIntakePageResponse(p.content().stream().map(VehicleIntakeResponse::from).toList(),
                p.page(), p.size(), p.totalElements(), p.totalPages());
    }
}
