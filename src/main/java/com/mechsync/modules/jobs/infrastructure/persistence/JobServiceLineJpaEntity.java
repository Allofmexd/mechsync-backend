package com.mechsync.modules.jobs.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "job_services")
public class JobServiceLineJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_job_services")
    private Long id;
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;
    @Column(name = "actual_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualUnitPrice;
    @Column(name = "actual_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualSubtotal;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected JobServiceLineJpaEntity() {
    }

    public JobServiceLineJpaEntity(Long jobId, Long serviceId, BigDecimal quantity,
            BigDecimal actualUnitPrice, BigDecimal actualSubtotal) {
        this.jobId = jobId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.actualUnitPrice = actualUnitPrice;
        this.actualSubtotal = actualSubtotal;
    }

    public void update(Long newServiceId, BigDecimal newQuantity, BigDecimal newUnitPrice,
            BigDecimal newSubtotal, LocalDateTime updateTime) {
        serviceId = newServiceId;
        quantity = newQuantity;
        actualUnitPrice = newUnitPrice;
        actualSubtotal = newSubtotal;
        updatedAt = updateTime;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getServiceId() { return serviceId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getActualUnitPrice() { return actualUnitPrice; }
    public BigDecimal getActualSubtotal() { return actualSubtotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
