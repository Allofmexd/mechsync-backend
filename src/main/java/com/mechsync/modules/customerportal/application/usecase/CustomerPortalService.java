package com.mechsync.modules.customerportal.application.usecase;

import com.mechsync.modules.customerportal.application.port.in.CustomerPortalQueryUseCase;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalQueryPort;
import com.mechsync.modules.customerportal.domain.exception.CustomerPortalVehicleNotFoundException;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehiclePage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleSummary;
import com.mechsync.modules.customers.application.port.in.ResolveAuthenticatedCustomerUseCase;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerPortalService implements CustomerPortalQueryUseCase {

    private final ResolveAuthenticatedCustomerUseCase authenticatedCustomerResolver;
    private final CustomerPortalQueryPort queryPort;

    public CustomerPortalService(
            ResolveAuthenticatedCustomerUseCase authenticatedCustomerResolver,
            CustomerPortalQueryPort queryPort) {
        this.authenticatedCustomerResolver = authenticatedCustomerResolver;
        this.queryPort = queryPort;
    }

    @Override
    public CustomerPortalProfile getProfile() {
        Long customerId = authenticatedCustomerResolver.resolveId();
        return queryPort.findProfileByCustomerId(customerId)
                .orElseThrow(CustomerIntegrityException::new);
    }

    @Override
    public CustomerPortalVehiclePage listVehicles(int page, int size) {
        Long customerId = authenticatedCustomerResolver.resolveId();
        VehiclePage result = queryPort.findVehiclesByCustomerId(customerId, page, size);
        return new CustomerPortalVehiclePage(
                result.content().stream().map(this::toSummary).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @Override
    public CustomerPortalVehicleDetail getVehicle(Long vehicleId) {
        Long customerId = authenticatedCustomerResolver.resolveId();
        Vehicle vehicle = queryPort.findVehicleByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(CustomerPortalVehicleNotFoundException::new);
        return new CustomerPortalVehicleDetail(
                vehicle.id(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.color(),
                vehicle.licensePlate(),
                vehicle.vin(),
                vehicle.currentMileage(),
                vehicle.createdAt());
    }

    private CustomerPortalVehicleSummary toSummary(Vehicle vehicle) {
        return new CustomerPortalVehicleSummary(
                vehicle.id(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.color(),
                vehicle.licensePlate(),
                VinMasker.mask(vehicle.vin()),
                vehicle.currentMileage(),
                String.format("%s %s %d", vehicle.brand(), vehicle.model(), vehicle.year()));
    }
}
