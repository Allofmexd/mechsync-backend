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
@Table(name = "job_parts")
public class JobPartLineJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_job_parts")
    private Long id;
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    @Column(name = "part_id", nullable = false)
    private Long partId;
    @Column(name = "quantity_used", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityUsed;
    @Column(name = "actual_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualUnitPrice;
    @Column(name = "actual_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualSubtotal;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected JobPartLineJpaEntity() {
    }

    public JobPartLineJpaEntity(Long jobId, Long partId, BigDecimal quantityUsed,
            BigDecimal actualUnitPrice, BigDecimal actualSubtotal) {
        this.jobId = jobId;
        this.partId = partId;
        this.quantityUsed = quantityUsed;
        this.actualUnitPrice = actualUnitPrice;
        this.actualSubtotal = actualSubtotal;
    }

    public void update(Long newPartId, BigDecimal newQuantity, BigDecimal newUnitPrice,
            BigDecimal newSubtotal, LocalDateTime updateTime) {
        partId = newPartId;
        quantityUsed = newQuantity;
        actualUnitPrice = newUnitPrice;
        actualSubtotal = newSubtotal;
        updatedAt = updateTime;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getPartId() { return partId; }
    public BigDecimal getQuantityUsed() { return quantityUsed; }
    public BigDecimal getActualUnitPrice() { return actualUnitPrice; }
    public BigDecimal getActualSubtotal() { return actualSubtotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
