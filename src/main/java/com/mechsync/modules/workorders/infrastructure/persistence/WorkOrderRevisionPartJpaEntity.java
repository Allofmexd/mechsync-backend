package com.mechsync.modules.workorders.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "work_order_revision_parts")
public class WorkOrderRevisionPartJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_work_order_revision_parts") private Long id;
    @Column(name = "work_order_revision_id", nullable = false) private Long revisionId;
    @Column(name = "line_number", nullable = false) private Integer lineNumber;
    @Column(name = "part_id") private Long partId;
    @Column(name = "part_name_snapshot", nullable = false, length = 150) private String nameSnapshot;
    @Column(name = "part_number_snapshot", length = 100) private String partNumberSnapshot;
    @Lob @Column(name = "part_description_snapshot", columnDefinition = "TEXT") private String descriptionSnapshot;
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6) private BigDecimal quantity;
    @Column(name = "unit_price_snapshot", nullable = false, precision = 19, scale = 4) private BigDecimal unitPrice;
    @Column(name = "line_total_snapshot", nullable = false, precision = 19, scale = 4) private BigDecimal lineSubtotal;
    @Lob @Column(name = "notes", columnDefinition = "TEXT") private String notes;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    protected WorkOrderRevisionPartJpaEntity() { }
    public WorkOrderRevisionPartJpaEntity(Long revisionId,Integer lineNumber,Long partId,String name,String partNumber,
            String description,BigDecimal quantity,BigDecimal unitPrice,BigDecimal subtotal,String notes){
        this.revisionId=revisionId;this.lineNumber=lineNumber;this.partId=partId;nameSnapshot=name;
        partNumberSnapshot=partNumber;descriptionSnapshot=description;this.quantity=quantity;this.unitPrice=unitPrice;
        lineSubtotal=subtotal;this.notes=notes;}
    public Long getId(){return id;} public Long getRevisionId(){return revisionId;}
    public Integer getLineNumber(){return lineNumber;} public Long getPartId(){return partId;}
    public String getNameSnapshot(){return nameSnapshot;} public String getPartNumberSnapshot(){return partNumberSnapshot;}
    public String getDescriptionSnapshot(){return descriptionSnapshot;} public BigDecimal getQuantity(){return quantity;}
    public BigDecimal getUnitPrice(){return unitPrice;} public BigDecimal getLineSubtotal(){return lineSubtotal;}
    public String getNotes(){return notes;} public LocalDateTime getCreatedAt(){return createdAt;}
}
