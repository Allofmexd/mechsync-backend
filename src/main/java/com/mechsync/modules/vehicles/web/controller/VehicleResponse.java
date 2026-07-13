package com.mechsync.modules.vehicles.web.controller;

import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        Long customerId,
        String brand,
        String model,
        Integer year,
        String color,
        String licensePlate,
        String vin,
        Integer currentMileage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.id(), vehicle.customerId(), vehicle.brand(), vehicle.model(), vehicle.year(),
                vehicle.color(), vehicle.licensePlate(), vehicle.vin(), vehicle.currentMileage(),
                vehicle.createdAt(), vehicle.updatedAt());
    }
}
