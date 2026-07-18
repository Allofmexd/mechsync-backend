package com.mechsync.modules.parts.application.port.out;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;

public interface PartCatalogRepositoryPort {

    PartCatalogPage findAll(int page, int size, String search);
}
