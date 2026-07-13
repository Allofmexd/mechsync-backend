package com.mechsync.modules.vehicleintakes.infrastructure.persistence;

import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
import com.mechsync.modules.vehicleintakes.application.port.out.VehicleIntakeRepositoryPort;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeInUseException;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import com.mechsync.modules.vehicleintakes.infrastructure.repository.VehicleIntakeJpaRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

@Component
public class VehicleIntakePersistenceAdapter implements VehicleIntakeRepositoryPort {
    private final VehicleIntakeJpaRepository repository;
    public VehicleIntakePersistenceAdapter(VehicleIntakeJpaRepository repository) { this.repository = repository; }

    @Override public VehicleIntakePage findAll(int page, int size) {
        Page<VehicleIntakeJpaEntity> result = repository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
        return new VehicleIntakePage(result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    @Override public Optional<VehicleIntake> findById(Long id) { return repository.findById(id).map(this::toDomain); }
    @Override public boolean vehicleExists(Long id) { return repository.countVehiclesById(id) > 0; }
    @Override public boolean technicianExists(Long id) { return repository.countTechniciansById(id) > 0; }
    @Override public boolean intakeStatusExists(Long id) { return repository.countIntakeStatusesById(id) > 0; }
    @Override public boolean hasWorkOrders(Long id) { return repository.countWorkOrdersByIntakeId(id) > 0; }
    @Override public VehicleIntake save(VehicleIntake intake) {
        return toDomain(repository.saveAndFlush(new VehicleIntakeJpaEntity(intake.id(), intake.vehicleId(),
                intake.technicianId(), intake.intakeDate(), intake.intakeMileage(), intake.reportedProblem(),
                intake.initialObservations(), intake.statusId(), intake.createdAt(), intake.updatedAt())));
    }
    @Override public void deleteById(Long id) {
        try { repository.deleteById(id); repository.flush(); }
        catch (DataIntegrityViolationException exception) { throw new VehicleIntakeInUseException(id); }
    }
    private VehicleIntake toDomain(VehicleIntakeJpaEntity e) {
        return new VehicleIntake(e.getId(), e.getVehicleId(), e.getTechnicianId(), e.getIntakeDate(),
                e.getIntakeMileage(), e.getReportedProblem(), e.getInitialObservations(), e.getStatusId(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
