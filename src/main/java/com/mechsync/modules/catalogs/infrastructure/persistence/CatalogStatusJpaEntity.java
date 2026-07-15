package com.mechsync.modules.catalogs.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "status_catalog")
public class CatalogStatusJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_catalog")
    private Long id;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "name", nullable = false)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    protected CatalogStatusJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getContext() {
        return context;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
