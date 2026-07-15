package com.mechsync.modules.catalogs.infrastructure.repository;

import com.mechsync.modules.catalogs.infrastructure.persistence.CatalogStatusJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogStatusJpaRepository extends JpaRepository<CatalogStatusJpaEntity, Long> {

    List<CatalogStatusJpaEntity> findAllByContextOrderByIdAsc(String context);
}
