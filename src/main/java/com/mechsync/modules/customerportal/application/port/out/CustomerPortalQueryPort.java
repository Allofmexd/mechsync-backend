package com.mechsync.modules.customerportal.application.port.out;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.util.Optional;

public interface CustomerPortalQueryPort {

    Optional<CustomerPortalProfile> findProfileByCustomerId(Long customerId);

    VehiclePage findVehiclesByCustomerId(Long customerId, int page, int size);

    Optional<Vehicle> findVehicleByIdAndCustomerId(Long vehicleId, Long customerId);
}
