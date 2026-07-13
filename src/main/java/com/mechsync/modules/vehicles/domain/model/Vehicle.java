package com.mechsync.modules.vehicles.domain.model;

import java.time.LocalDateTime;

public record Vehicle(
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
}
