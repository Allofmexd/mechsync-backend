package com.mechsync.modules.technicians.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "technicians")
public class TechnicianJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_technicians")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    protected TechnicianJpaEntity() {
    }
}
