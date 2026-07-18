package com.mechsync.modules.jobs.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "jobs")
public class JobJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jobs")
    private Long id;
    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;
    @Column(name = "initial_approved_revision_id")
    private Long initialApprovedRevisionId;
    @Column(name = "technician_id", nullable = false)
    private Long technicianId;
    @Column(name = "scheduled_start_date")
    private LocalDateTime scheduledStartDate;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "actual_hours", precision = 5, scale = 2)
    private BigDecimal actualHours;
    @Column(name = "actual_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualSubtotal;
    @Column(name = "actual_iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualIva;
    @Column(name = "actual_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualTotal;
    @Lob
    @Column(name = "execution_observations", columnDefinition = "TEXT")
    private String executionObservations;
    @Column(name = "cancellation_notes", length = 500)
    private String cancellationNotes;
    @Column(name = "status_id", nullable = false)
    private Long statusId;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected JobJpaEntity() {
    }

    public JobJpaEntity(Long workOrderId, Long initialApprovedRevisionId, Long technicianId,
            LocalDateTime scheduledStartDate, BigDecimal actualSubtotal, BigDecimal actualIva,
            BigDecimal actualTotal, String executionObservations, Long statusId) {
        this.workOrderId = workOrderId;
        this.initialApprovedRevisionId = initialApprovedRevisionId;
        this.technicianId = technicianId;
        this.scheduledStartDate = scheduledStartDate;
        this.actualSubtotal = actualSubtotal;
        this.actualIva = actualIva;
        this.actualTotal = actualTotal;
        this.executionObservations = executionObservations;
        this.statusId = statusId;
    }

    public void applyWorkflow(LocalDateTime start, LocalDateTime completion,
            LocalDateTime cancellation, BigDecimal subtotal, BigDecimal iva, BigDecimal total,
            String observations, String cancellationReason, Long newStatusId,
            LocalDateTime updateTime) {
        startDate = start;
        completionDate = completion;
        cancelledAt = cancellation;
        actualSubtotal = subtotal;
        actualIva = iva;
        actualTotal = total;
        executionObservations = observations;
        cancellationNotes = cancellationReason;
        statusId = newStatusId;
        updatedAt = updateTime;
    }

    public Long getId() { return id; }
    public Long getWorkOrderId() { return workOrderId; }
    public Long getInitialApprovedRevisionId() { return initialApprovedRevisionId; }
    public Long getTechnicianId() { return technicianId; }
    public LocalDateTime getScheduledStartDate() { return scheduledStartDate; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getCompletionDate() { return completionDate; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public BigDecimal getActualHours() { return actualHours; }
    public BigDecimal getActualSubtotal() { return actualSubtotal; }
    public BigDecimal getActualIva() { return actualIva; }
    public BigDecimal getActualTotal() { return actualTotal; }
    public String getExecutionObservations() { return executionObservations; }
    public String getCancellationNotes() { return cancellationNotes; }
    public Long getStatusId() { return statusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
