package com.mechsync.modules.users.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "roles")
public class RoleJpaEntity {

    @Id
    @Column(name = "id_roles")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected RoleJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
