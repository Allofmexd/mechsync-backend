package com.mechsync.modules.workorders.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "work_order_revisions")
public class WorkOrderRevisionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_work_order_revisions")
    private Long id;
    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;
    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;
    @Column(name = "revision_status_id", nullable = false)
    private Long revisionStatusId;
    @Column(name = "technician_id", nullable = false)
    private Long technicianId;
    @Column(name = "estimated_start_date")
    private LocalDateTime estimatedStartDate;
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    @Column(name = "estimated_hours", precision = 10, scale = 4)
    private BigDecimal estimatedHours;
    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotalAmount;
    @Column(name = "apply_iva", nullable = false)
    private boolean applyIva;
    @Column(name = "iva_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal ivaRate;
    @Column(name = "iva_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal ivaAmount;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Lob
    @Column(name = "tax_treatment_notes", columnDefinition = "TEXT")
    private String taxTreatmentNotes;
    @Lob
    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;
    @Lob
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;
    @Column(name = "change_reason", length = 500)
    private String changeReason;
    @Column(name = "created_by_user_id")
    private Long createdByUserId;
    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "accepted_by_name", length = 200)
    private String acceptedByName;
    @Column(name = "accepted_by_user_id")
    private Long acceptedByUserId;
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    @Column(name = "acceptance_method_id")
    private Long acceptanceMethodId;
    @Lob
    @Column(name = "acceptance_notes", columnDefinition = "TEXT")
    private String acceptanceNotes;
    @Column(name = "is_migrated", nullable = false)
    private boolean migrated;
    @Column(name = "migration_notes", length = 500)
    private String migrationNotes;
    @Column(name = "lock_version", nullable = false)
    private long lockVersion;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "workflow_updated_at")
    private LocalDateTime workflowUpdatedAt;

    protected WorkOrderRevisionJpaEntity() {
    }

    public WorkOrderRevisionJpaEntity(
            Long workOrderId,
            Integer revisionNumber,
            Long revisionStatusId,
            Long technicianId,
            LocalDateTime estimatedStartDate,
            LocalDateTime estimatedDeliveryDate,
            BigDecimal estimatedHours,
            BigDecimal subtotalAmount,
            boolean applyIva,
            BigDecimal ivaRate,
            BigDecimal ivaAmount,
            BigDecimal totalAmount,
            String currency,
            String taxTreatmentNotes,
            String technicalNotes,
            String customerNotes,
            String changeReason,
            Long createdByUserId) {
        this.workOrderId = workOrderId;
        this.revisionNumber = revisionNumber;
        this.revisionStatusId = revisionStatusId;
        this.technicianId = technicianId;
        this.estimatedStartDate = estimatedStartDate;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.estimatedHours = estimatedHours;
        this.subtotalAmount = subtotalAmount;
        this.applyIva = applyIva;
        this.ivaRate = ivaRate;
        this.ivaAmount = ivaAmount;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.taxTreatmentNotes = taxTreatmentNotes;
        this.technicalNotes = technicalNotes;
        this.customerNotes = customerNotes;
        this.changeReason = changeReason;
        this.createdByUserId = createdByUserId;
    }

    public void transitionTo(Long statusId, LocalDateTime changedAt) {
        revisionStatusId = statusId;
        workflowUpdatedAt = changedAt;
        lockVersion++;
    }

    public void approve(
            Long statusId,
            Long approvedBy,
            LocalDateTime approvedTime,
            String acceptedName,
            Long acceptedUser,
            LocalDateTime acceptedTime,
            Long methodId,
            String notes) {
        revisionStatusId = statusId;
        approvedByUserId = approvedBy;
        approvedAt = approvedTime;
        acceptedByName = acceptedName;
        acceptedByUserId = acceptedUser;
        acceptedAt = acceptedTime;
        acceptanceMethodId = methodId;
        acceptanceNotes = notes;
        workflowUpdatedAt = approvedTime;
        lockVersion++;
    }

    public Long getId() { return id; }
    public Long getWorkOrderId() { return workOrderId; }
    public Integer getRevisionNumber() { return revisionNumber; }
    public Long getRevisionStatusId() { return revisionStatusId; }
    public Long getTechnicianId() { return technicianId; }
    public LocalDateTime getEstimatedStartDate() { return estimatedStartDate; }
    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public BigDecimal getEstimatedHours() { return estimatedHours; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public boolean isApplyIva() { return applyIva; }
    public BigDecimal getIvaRate() { return ivaRate; }
    public BigDecimal getIvaAmount() { return ivaAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getTaxTreatmentNotes() { return taxTreatmentNotes; }
    public String getTechnicalNotes() { return technicalNotes; }
    public String getCustomerNotes() { return customerNotes; }
    public String getChangeReason() { return changeReason; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public Long getApprovedByUserId() { return approvedByUserId; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getAcceptedByName() { return acceptedByName; }
    public Long getAcceptedByUserId() { return acceptedByUserId; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public Long getAcceptanceMethodId() { return acceptanceMethodId; }
    public String getAcceptanceNotes() { return acceptanceNotes; }
    public long getLockVersion() { return lockVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getWorkflowUpdatedAt() { return workflowUpdatedAt; }
}
