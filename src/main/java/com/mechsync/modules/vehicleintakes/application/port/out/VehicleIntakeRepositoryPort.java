package com.mechsync.modules.vehicleintakes.application.port.out;

import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import java.util.Optional;

public interface VehicleIntakeRepositoryPort {
    VehicleIntakePage findAll(int page, int size);
    Optional<VehicleIntake> findById(Long intakeId);
    boolean vehicleExists(Long vehicleId);
    boolean technicianExists(Long technicianId);
    boolean intakeStatusExists(Long statusId);
    boolean hasWorkOrders(Long intakeId);
    VehicleIntake save(VehicleIntake intake);
    void deleteById(Long intakeId);
}
