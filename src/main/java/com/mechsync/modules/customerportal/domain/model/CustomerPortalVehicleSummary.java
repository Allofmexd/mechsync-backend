package com.mechsync.modules.customerportal.domain.model;

public record CustomerPortalVehicleSummary(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        String color,
        String licensePlate,
        String maskedVin,
        Integer currentMileage,
        String description) {
}
