package com.mechsync.modules.servicereports.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_reports")
public class ServiceReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_reports")
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "report_date", nullable = false, updatable = false)
    private LocalDateTime reportDate;

    @Column(name = "final_description", nullable = false, columnDefinition = "TEXT")
    private String finalDescription;

    @Column(name = "final_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalSubtotal;

    @Column(name = "final_iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalIva;

    @Column(name = "final_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalTotal;

    @Column(name = "customer_confirmation", nullable = false)
    private boolean customerConfirmation;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ServiceReportJpaEntity() {
    }

    public ServiceReportJpaEntity(Long jobId, LocalDateTime reportDate, String finalDescription,
            BigDecimal finalSubtotal, BigDecimal finalIva, BigDecimal finalTotal,
            boolean customerConfirmation, LocalDateTime deliveredAt, Long statusId,
            LocalDateTime createdAt) {
        this.jobId = jobId;
        this.reportDate = reportDate;
        this.finalDescription = finalDescription;
        this.finalSubtotal = finalSubtotal;
        this.finalIva = finalIva;
        this.finalTotal = finalTotal;
        this.customerConfirmation = customerConfirmation;
        this.deliveredAt = deliveredAt;
        this.statusId = statusId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public LocalDateTime getReportDate() { return reportDate; }
    public String getFinalDescription() { return finalDescription; }
    public BigDecimal getFinalSubtotal() { return finalSubtotal; }
    public BigDecimal getFinalIva() { return finalIva; }
    public BigDecimal getFinalTotal() { return finalTotal; }
    public boolean isCustomerConfirmation() { return customerConfirmation; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public Long getStatusId() { return statusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
