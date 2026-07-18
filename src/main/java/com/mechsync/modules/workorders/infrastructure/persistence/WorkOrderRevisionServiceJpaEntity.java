package com.mechsync.modules.workorders.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "work_order_revision_services")
public class WorkOrderRevisionServiceJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_work_order_revision_services") private Long id;
    @Column(name = "work_order_revision_id", nullable = false) private Long revisionId;
    @Column(name = "line_number", nullable = false) private Integer lineNumber;
    @Column(name = "service_id") private Long serviceId;
    @Column(name = "service_name_snapshot", nullable = false, length = 150) private String nameSnapshot;
    @Lob @Column(name = "service_description_snapshot", columnDefinition = "TEXT") private String descriptionSnapshot;
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6) private BigDecimal quantity;
    @Column(name = "unit_price_snapshot", nullable = false, precision = 19, scale = 4) private BigDecimal unitPrice;
    @Column(name = "line_total_snapshot", nullable = false, precision = 19, scale = 4) private BigDecimal lineSubtotal;
    @Lob @Column(name = "notes", columnDefinition = "TEXT") private String notes;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    protected WorkOrderRevisionServiceJpaEntity() { }
    public WorkOrderRevisionServiceJpaEntity(Long revisionId,Integer lineNumber,Long serviceId,String name,
            String description,BigDecimal quantity,BigDecimal unitPrice,BigDecimal subtotal,String notes){
        this.revisionId=revisionId;this.lineNumber=lineNumber;this.serviceId=serviceId;nameSnapshot=name;
        descriptionSnapshot=description;this.quantity=quantity;this.unitPrice=unitPrice;lineSubtotal=subtotal;this.notes=notes;}
    public Long getId(){return id;} public Long getRevisionId(){return revisionId;}
    public Integer getLineNumber(){return lineNumber;} public Long getServiceId(){return serviceId;}
    public String getNameSnapshot(){return nameSnapshot;} public String getDescriptionSnapshot(){return descriptionSnapshot;}
    public BigDecimal getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;}
    public BigDecimal getLineSubtotal(){return lineSubtotal;} public String getNotes(){return notes;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
