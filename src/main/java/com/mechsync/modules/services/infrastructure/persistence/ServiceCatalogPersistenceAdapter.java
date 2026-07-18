package com.mechsync.modules.services.infrastructure.persistence;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import com.mechsync.modules.services.application.port.out.ServiceCatalogRepositoryPort;
import com.mechsync.modules.services.domain.model.ServiceCatalogItem;
import com.mechsync.modules.services.infrastructure.repository.ServiceCatalogJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ServiceCatalogPersistenceAdapter implements ServiceCatalogRepositoryPort {

    private static final Sort STABLE_SORT = Sort.by(Sort.Direction.ASC, "name")
            .and(Sort.by(Sort.Direction.ASC, "id"));

    private final ServiceCatalogJpaRepository repository;

    public ServiceCatalogPersistenceAdapter(ServiceCatalogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceCatalogPage findAll(int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, STABLE_SORT);
        Page<ServiceCatalogJpaEntity> result = search == null
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        search, search, pageable);
        return new ServiceCatalogPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private ServiceCatalogItem toDomain(ServiceCatalogJpaEntity entity) {
        return new ServiceCatalogItem(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getBasePrice(), entity.getEstimatedHours(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
