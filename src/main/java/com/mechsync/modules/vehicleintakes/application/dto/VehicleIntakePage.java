package com.mechsync.modules.vehicleintakes.application.dto;

import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import java.util.List;

public record VehicleIntakePage(
        List<VehicleIntake> content, int page, int size, long totalElements, int totalPages) {
}
