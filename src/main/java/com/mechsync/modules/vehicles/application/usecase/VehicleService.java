package com.mechsync.modules.vehicles.application.usecase;

import com.mechsync.modules.vehicles.application.dto.CreateVehicleCommand;
import com.mechsync.modules.vehicles.application.dto.UpdateVehicleCommand;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.application.port.in.CreateVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.DeleteVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.GetVehicleByIdUseCase;
import com.mechsync.modules.vehicles.application.port.in.ListVehiclesUseCase;
import com.mechsync.modules.vehicles.application.port.in.UpdateVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.out.VehicleRepositoryPort;
import com.mechsync.modules.vehicles.domain.exception.DuplicateVehicleException;
import com.mechsync.modules.vehicles.domain.exception.InvalidVehicleYearException;
import com.mechsync.modules.vehicles.domain.exception.VehicleCustomerNotFoundException;
import com.mechsync.modules.vehicles.domain.exception.VehicleInUseException;
import com.mechsync.modules.vehicles.domain.exception.VehicleNotFoundException;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VehicleService implements
        ListVehiclesUseCase,
        GetVehicleByIdUseCase,
        CreateVehicleUseCase,
        UpdateVehicleUseCase,
        DeleteVehicleUseCase {

    private static final int MINIMUM_YEAR = 1900;

    private final VehicleRepositoryPort vehicleRepository;

    public VehicleService(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehiclePage list(int page, int size) {
        return vehicleRepository.findAll(page, size);
    }

    @Override
    public Vehicle getById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    @Override
    @Transactional
    public Vehicle create(CreateVehicleCommand command) {
        validateYear(command.year());
        if (!vehicleRepository.customerExists(command.customerId())) {
            throw new VehicleCustomerNotFoundException(command.customerId());
        }
        String licensePlate = normalizeIdentifier(command.licensePlate());
        String vin = normalizeOptionalIdentifier(command.vin());
        if (vehicleRepository.existsByLicensePlate(licensePlate)
                || (vin != null && vehicleRepository.existsByVin(vin))) {
            throw new DuplicateVehicleException();
        }
        return vehicleRepository.save(new Vehicle(
                null,
                command.customerId(),
                command.brand().trim(),
                command.model().trim(),
                command.year(),
                normalizeOptional(command.color()),
                licensePlate,
                vin,
                command.currentMileage(),
                null,
                null));
    }

    @Override
    @Transactional
    public Vehicle update(UpdateVehicleCommand command) {
        Vehicle current = getById(command.vehicleId());
        validateYear(command.year());
        String licensePlate = normalizeIdentifier(command.licensePlate());
        String vin = normalizeOptionalIdentifier(command.vin());
        if (vehicleRepository.existsByLicensePlateExcludingId(licensePlate, current.id())
                || (vin != null && vehicleRepository.existsByVinExcludingId(vin, current.id()))) {
            throw new DuplicateVehicleException();
        }
        return vehicleRepository.save(new Vehicle(
                current.id(),
                current.customerId(),
                command.brand().trim(),
                command.model().trim(),
                command.year(),
                normalizeOptional(command.color()),
                licensePlate,
                vin,
                command.currentMileage(),
                current.createdAt(),
                LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void delete(Long vehicleId) {
        getById(vehicleId);
        if (vehicleRepository.hasIntakes(vehicleId)) {
            throw new VehicleInUseException(vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }

    private void validateYear(Integer vehicleYear) {
        int maximumYear = Year.now().getValue() + 1;
        if (vehicleYear < MINIMUM_YEAR || vehicleYear > maximumYear) {
            throw new InvalidVehicleYearException(MINIMUM_YEAR, maximumYear);
        }
    }

    private String normalizeIdentifier(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalIdentifier(String value) {
        return value == null ? null : normalizeIdentifier(value);
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }
}
