package com.mechsync.modules.vehicles.infrastructure.persistence;

import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.application.port.out.VehicleRepositoryPort;
import com.mechsync.modules.vehicles.domain.exception.DuplicateVehicleException;
import com.mechsync.modules.vehicles.domain.exception.VehicleInUseException;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import com.mechsync.modules.vehicles.infrastructure.repository.VehicleJpaRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class VehiclePersistenceAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository repository;

    public VehiclePersistenceAdapter(VehicleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public VehiclePage findAll(int page, int size) {
        Page<VehicleJpaEntity> result = repository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
        return new VehiclePage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Optional<Vehicle> findById(Long vehicleId) {
        return repository.findById(vehicleId).map(this::toDomain);
    }

    @Override
    public boolean customerExists(Long customerId) {
        return repository.countCustomersById(customerId) > 0;
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return repository.existsByLicensePlateIgnoreCase(licensePlate);
    }

    @Override
    public boolean existsByVin(String vin) {
        return repository.existsByVinIgnoreCase(vin);
    }

    @Override
    public boolean existsByLicensePlateExcludingId(String licensePlate, Long vehicleId) {
        return repository.existsByLicensePlateIgnoreCaseAndIdNot(licensePlate, vehicleId);
    }

    @Override
    public boolean existsByVinExcludingId(String vin, Long vehicleId) {
        return repository.existsByVinIgnoreCaseAndIdNot(vin, vehicleId);
    }

    @Override
    public boolean hasIntakes(Long vehicleId) {
        return repository.countIntakesByVehicleId(vehicleId) > 0;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        try {
            return toDomain(repository.saveAndFlush(new VehicleJpaEntity(
                    vehicle.id(), vehicle.customerId(), vehicle.brand(), vehicle.model(), vehicle.year(),
                    vehicle.color(), vehicle.licensePlate(), vehicle.vin(), vehicle.currentMileage(),
                    vehicle.createdAt(), vehicle.updatedAt())));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateVehicleException();
        }
    }

    @Override
    public void deleteById(Long vehicleId) {
        try {
            repository.deleteById(vehicleId);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new VehicleInUseException(vehicleId);
        }
    }

    private Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(
                entity.getId(), entity.getCustomerId(), entity.getBrand(), entity.getModel(),
                entity.getYear(), entity.getColor(), entity.getLicensePlate(), entity.getVin(),
                entity.getCurrentMileage(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
