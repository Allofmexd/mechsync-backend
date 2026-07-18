package com.mechsync.modules.parts.infrastructure.persistence;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import com.mechsync.modules.parts.application.port.out.PartCatalogRepositoryPort;
import com.mechsync.modules.parts.domain.model.PartCatalogItem;
import com.mechsync.modules.parts.infrastructure.repository.PartCatalogJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PartCatalogPersistenceAdapter implements PartCatalogRepositoryPort {

    private static final Sort STABLE_SORT = Sort.by(Sort.Direction.ASC, "name")
            .and(Sort.by(Sort.Direction.ASC, "id"));

    private final PartCatalogJpaRepository repository;

    public PartCatalogPersistenceAdapter(PartCatalogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartCatalogPage findAll(int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, STABLE_SORT);
        Page<PartCatalogJpaEntity> result = search == null
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        search, search, pageable);
        return new PartCatalogPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private PartCatalogItem toDomain(PartCatalogJpaEntity entity) {
        MeasurementUnitJpaEntity unit = entity.getMeasurementUnit();
        return new PartCatalogItem(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getUnitPrice(), unit.getId(), unit.getName(), unit.getAbbreviation(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
