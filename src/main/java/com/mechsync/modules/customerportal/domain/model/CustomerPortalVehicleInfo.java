package com.mechsync.modules.customerportal.domain.model;

public record CustomerPortalVehicleInfo(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        String licensePlate,
        String label) {
}
