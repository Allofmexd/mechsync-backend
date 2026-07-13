package com.mechsync.modules.vehicles.application.dto;

public record CreateVehicleCommand(
        Long customerId,
        String brand,
        String model,
        Integer year,
        String color,
        String licensePlate,
        String vin,
        Integer currentMileage) {
}
