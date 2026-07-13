package com.mechsync.modules.vehicles.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicles")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "brand", nullable = false, length = 80)
    private String brand;

    @Column(name = "model", nullable = false, length = 80)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "vin", unique = true, length = 100)
    private String vin;

    @Column(name = "current_mileage")
    private Integer currentMileage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected VehicleJpaEntity() {
    }

    public VehicleJpaEntity(
            Long id,
            Long customerId,
            String brand,
            String model,
            Integer year,
            String color,
            String licensePlate,
            String vin,
            Integer currentMileage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.licensePlate = licensePlate;
        this.vin = vin;
        this.currentMileage = currentMileage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public String getColor() { return color; }
    public String getLicensePlate() { return licensePlate; }
    public String getVin() { return vin; }
    public Integer getCurrentMileage() { return currentMileage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
