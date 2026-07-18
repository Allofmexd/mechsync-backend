package com.mechsync.modules.parts.application.port.in;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;

public interface ListPartsUseCase {

    PartCatalogPage list(int page, int size, String search);
}
