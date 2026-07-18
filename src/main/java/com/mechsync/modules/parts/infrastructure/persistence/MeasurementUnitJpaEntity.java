package com.mechsync.modules.parts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "measurement_units")
public class MeasurementUnitJpaEntity {

    @Id
    @Column(name = "id_measurement_units")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "abbreviation", length = 20)
    private String abbreviation;

    protected MeasurementUnitJpaEntity() {
    }

    public MeasurementUnitJpaEntity(Long id, String name, String abbreviation) {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
