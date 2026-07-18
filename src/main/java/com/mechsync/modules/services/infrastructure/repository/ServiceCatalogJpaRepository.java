package com.mechsync.modules.services.infrastructure.repository;

import com.mechsync.modules.services.infrastructure.persistence.ServiceCatalogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCatalogJpaRepository
        extends JpaRepository<ServiceCatalogJpaEntity, Long> {

    Page<ServiceCatalogJpaEntity> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);
}
