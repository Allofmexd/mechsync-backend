package com.mechsync.modules.workorders.infrastructure.persistence;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
@Entity @Table(name="work_orders")
public class WorkOrderJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_work_orders") private Long id;
 @Column(name="vehicle_intake_id",nullable=false) private Long vehicleIntakeId;
 @Column(name="technician_id",nullable=false) private Long technicianId;
 @Column(name="work_order_date") private LocalDateTime workOrderDate;
 @Column(name="estimated_start_date") private LocalDateTime estimatedStartDate;
 @Column(name="estimated_delivery_date") private LocalDateTime estimatedDeliveryDate;
 @Column(name="estimated_hours",precision=5,scale=2) private BigDecimal estimatedHours;
 @Column(name="estimated_subtotal",nullable=false,precision=10,scale=2) private BigDecimal estimatedSubtotal;
 @Column(name="estimated_iva",nullable=false,precision=10,scale=2) private BigDecimal estimatedIva;
 @Column(name="estimated_total",nullable=false,precision=10,scale=2) private BigDecimal estimatedTotal;
 @Lob @Column(name="technical_observations",columnDefinition="TEXT") private String technicalObservations;
 @Column(name="status_id",nullable=false) private Long statusId;
 @Column(name="current_revision_id") private Long currentRevisionId;
 @Column(name="final_approved_revision_id") private Long finalApprovedRevisionId;
 @Column(name="lock_version",nullable=false) private long lockVersion;
 @Column(name="created_by_user_id") private Long createdByUserId;
 @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_at") private LocalDateTime updatedAt;
 protected WorkOrderJpaEntity() { }
 public WorkOrderJpaEntity(Long id,Long intake,Long technician,LocalDateTime date,LocalDateTime start,
   LocalDateTime delivery,BigDecimal hours,BigDecimal subtotal,BigDecimal iva,BigDecimal total,
   String observations,Long status,LocalDateTime created,LocalDateTime updated) {
  this.id=id;vehicleIntakeId=intake;technicianId=technician;workOrderDate=date;estimatedStartDate=start;
  estimatedDeliveryDate=delivery;estimatedHours=hours;estimatedSubtotal=subtotal;estimatedIva=iva;
  estimatedTotal=total;technicalObservations=observations;statusId=status;createdAt=created;updatedAt=updated;
 }
 public void updateLegacyFields(Long technician,LocalDateTime date,LocalDateTime start,
   LocalDateTime delivery,BigDecimal hours,BigDecimal subtotal,BigDecimal iva,BigDecimal total,
   String observations,Long status,LocalDateTime updated){technicianId=technician;workOrderDate=date;
  estimatedStartDate=start;estimatedDeliveryDate=delivery;estimatedHours=hours;estimatedSubtotal=subtotal;
  estimatedIva=iva;estimatedTotal=total;technicalObservations=observations;statusId=status;updatedAt=updated;}
 public void setCurrentRevisionId(Long revisionId){currentRevisionId=revisionId;lockVersion++;}
 public void setFinalApprovedRevisionId(Long revisionId){finalApprovedRevisionId=revisionId;lockVersion++;}
 public Long getId(){return id;} public Long getVehicleIntakeId(){return vehicleIntakeId;}
 public Long getTechnicianId(){return technicianId;} public LocalDateTime getWorkOrderDate(){return workOrderDate;}
 public LocalDateTime getEstimatedStartDate(){return estimatedStartDate;}
 public LocalDateTime getEstimatedDeliveryDate(){return estimatedDeliveryDate;}
 public BigDecimal getEstimatedHours(){return estimatedHours;} public BigDecimal getEstimatedSubtotal(){return estimatedSubtotal;}
 public BigDecimal getEstimatedIva(){return estimatedIva;} public BigDecimal getEstimatedTotal(){return estimatedTotal;}
 public String getTechnicalObservations(){return technicalObservations;} public Long getStatusId(){return statusId;}
 public Long getCurrentRevisionId(){return currentRevisionId;}
 public Long getFinalApprovedRevisionId(){return finalApprovedRevisionId;}
 public long getLockVersion(){return lockVersion;} public Long getCreatedByUserId(){return createdByUserId;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
