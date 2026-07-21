package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;

public record CustomerPortalVehicleDetail(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        String color,
        String licensePlate,
        String vin,
        Integer currentMileage,
        LocalDateTime createdAt) {
}
