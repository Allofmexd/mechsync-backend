package com.mechsync.modules.vehicles.application.port.out;

import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.util.Optional;

public interface VehicleRepositoryPort {
    VehiclePage findAll(int page, int size);
    Optional<Vehicle> findById(Long vehicleId);
    boolean customerExists(Long customerId);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByVin(String vin);
    boolean existsByLicensePlateExcludingId(String licensePlate, Long vehicleId);
    boolean existsByVinExcludingId(String vin, Long vehicleId);
    boolean hasIntakes(Long vehicleId);
    Vehicle save(Vehicle vehicle);
    void deleteById(Long vehicleId);
}
