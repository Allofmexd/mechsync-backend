package com.mechsync.modules.services.application.port.out;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;

public interface ServiceCatalogRepositoryPort {

    ServiceCatalogPage findAll(int page, int size, String search);
}
