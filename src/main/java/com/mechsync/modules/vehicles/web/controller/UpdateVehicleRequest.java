package com.mechsync.modules.vehicles.web.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
        @NotBlank @Size(max = 80) String brand,
        @NotBlank @Size(max = 80) String model,
        @NotNull @Min(1900) Integer year,
        @Size(max = 50) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String color,
        @NotBlank @Size(max = 20) String licensePlate,
        @Size(max = 100) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String vin,
        @PositiveOrZero Integer currentMileage) {
}
