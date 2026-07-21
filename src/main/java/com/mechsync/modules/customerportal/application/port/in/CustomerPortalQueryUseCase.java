package com.mechsync.modules.customerportal.application.port.in;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehiclePage;

public interface CustomerPortalQueryUseCase {

    CustomerPortalProfile getProfile();

    CustomerPortalVehiclePage listVehicles(int page, int size);

    CustomerPortalVehicleDetail getVehicle(Long vehicleId);
}
