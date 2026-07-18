package com.mechsync.modules.services.application.port.in;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;

public interface ListServicesUseCase {

    ServiceCatalogPage list(int page, int size, String search);
}
