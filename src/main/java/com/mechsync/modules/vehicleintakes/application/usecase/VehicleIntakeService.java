package com.mechsync.modules.vehicleintakes.application.usecase;

import com.mechsync.modules.vehicleintakes.application.dto.CreateVehicleIntakeCommand;
import com.mechsync.modules.vehicleintakes.application.dto.UpdateVehicleIntakeCommand;
import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
import com.mechsync.modules.vehicleintakes.application.port.in.*;
import com.mechsync.modules.vehicleintakes.application.port.out.VehicleIntakeRepositoryPort;
import com.mechsync.modules.vehicleintakes.domain.exception.*;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VehicleIntakeService implements ListVehicleIntakesUseCase, GetVehicleIntakeByIdUseCase,
        CreateVehicleIntakeUseCase, UpdateVehicleIntakeUseCase, DeleteVehicleIntakeUseCase {

    private final VehicleIntakeRepositoryPort repository;

    public VehicleIntakeService(VehicleIntakeRepositoryPort repository) { this.repository = repository; }

    @Override public VehicleIntakePage list(int page, int size) { return repository.findAll(page, size); }

    @Override public VehicleIntake getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new VehicleIntakeNotFoundException(id));
    }

    @Override @Transactional
    public VehicleIntake create(CreateVehicleIntakeCommand command) {
        validateReferences(command.vehicleId(), command.technicianId(), command.statusId());
        return repository.save(new VehicleIntake(null, command.vehicleId(), command.technicianId(),
                command.intakeDate() == null ? LocalDateTime.now() : command.intakeDate(),
                command.intakeMileage(), command.reportedProblem().trim(),
                trimOptional(command.initialObservations()), command.statusId(), null, null));
    }

    @Override @Transactional
    public VehicleIntake update(UpdateVehicleIntakeCommand command) {
        VehicleIntake current = getById(command.intakeId());
        validateReferences(current.vehicleId(), command.technicianId(), command.statusId());
        return repository.save(new VehicleIntake(current.id(), current.vehicleId(), command.technicianId(),
                command.intakeDate() == null ? current.intakeDate() : command.intakeDate(),
                command.intakeMileage(), command.reportedProblem().trim(),
                trimOptional(command.initialObservations()), command.statusId(), current.createdAt(),
                LocalDateTime.now()));
    }

    @Override @Transactional
    public void delete(Long id) {
        getById(id);
        if (repository.hasWorkOrders(id)) throw new VehicleIntakeInUseException(id);
        repository.deleteById(id);
    }

    private void validateReferences(Long vehicleId, Long technicianId, Long statusId) {
        if (!repository.vehicleExists(vehicleId)) throw new VehicleIntakeVehicleNotFoundException(vehicleId);
        if (technicianId != null && !repository.technicianExists(technicianId)) {
            throw new VehicleIntakeTechnicianNotFoundException(technicianId);
        }
        if (!repository.intakeStatusExists(statusId)) throw new VehicleIntakeStatusNotFoundException(statusId);
    }

    private String trimOptional(String value) { return value == null ? null : value.trim(); }
}
