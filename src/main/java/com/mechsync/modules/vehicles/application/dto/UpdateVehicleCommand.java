package com.mechsync.modules.vehicles.application.dto;

public record UpdateVehicleCommand(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        String color,
        String licensePlate,
        String vin,
        Integer currentMileage) {
}
