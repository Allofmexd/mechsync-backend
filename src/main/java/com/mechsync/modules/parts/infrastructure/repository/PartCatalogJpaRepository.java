package com.mechsync.modules.parts.infrastructure.repository;

import com.mechsync.modules.parts.infrastructure.persistence.PartCatalogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartCatalogJpaRepository extends JpaRepository<PartCatalogJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "measurementUnit")
    Page<PartCatalogJpaEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "measurementUnit")
    Page<PartCatalogJpaEntity> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);
}
