package com.mechsync.modules.workorders.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_acceptance_method_catalog")
public class WorkOrderAcceptanceMethodJpaEntity {
    @Id @Column(name = "id_work_order_acceptance_method_catalog") private Long id;
    @Column(name = "code", nullable = false, unique = true, length = 30) private String code;
    protected WorkOrderAcceptanceMethodJpaEntity() { }
    public Long getId(){return id;} public String getCode(){return code;}
}
