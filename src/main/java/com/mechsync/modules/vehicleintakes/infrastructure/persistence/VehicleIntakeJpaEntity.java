package com.mechsync.modules.vehicleintakes.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "vehicle_intakes")
public class VehicleIntakeJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_intakes") private Long id;
    @Column(name = "vehicle_id", nullable = false) private Long vehicleId;
    @Column(name = "technician_id") private Long technicianId;
    @Column(name = "intake_date") private LocalDateTime intakeDate;
    @Column(name = "intake_mileage") private Integer intakeMileage;
    @Lob @Column(name = "reported_problem", nullable = false, columnDefinition = "TEXT")
    private String reportedProblem;
    @Lob @Column(name = "initial_observations", columnDefinition = "TEXT")
    private String initialObservations;
    @Column(name = "status_id", nullable = false) private Long statusId;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    protected VehicleIntakeJpaEntity() { }

    public VehicleIntakeJpaEntity(Long id, Long vehicleId, Long technicianId, LocalDateTime intakeDate,
            Integer intakeMileage, String reportedProblem, String initialObservations, Long statusId,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.vehicleId = vehicleId; this.technicianId = technicianId;
        this.intakeDate = intakeDate; this.intakeMileage = intakeMileage;
        this.reportedProblem = reportedProblem; this.initialObservations = initialObservations;
        this.statusId = statusId; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public Long getTechnicianId() { return technicianId; }
    public LocalDateTime getIntakeDate() { return intakeDate; }
    public Integer getIntakeMileage() { return intakeMileage; }
    public String getReportedProblem() { return reportedProblem; }
    public String getInitialObservations() { return initialObservations; }
    public Long getStatusId() { return statusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
