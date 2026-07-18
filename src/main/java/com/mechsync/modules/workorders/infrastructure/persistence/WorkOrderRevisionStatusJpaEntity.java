package com.mechsync.modules.workorders.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_revision_status_catalog")
public class WorkOrderRevisionStatusJpaEntity {
    @Id @Column(name = "id_work_order_revision_status_catalog") private Long id;
    @Column(name = "code", nullable = false, unique = true, length = 30) private String code;
    protected WorkOrderRevisionStatusJpaEntity() { }
    public Long getId(){return id;} public String getCode(){return code;}
}
