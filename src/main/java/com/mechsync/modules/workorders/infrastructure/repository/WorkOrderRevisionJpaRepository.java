package com.mechsync.modules.workorders.infrastructure.repository;

import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderRevisionJpaEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkOrderRevisionJpaRepository extends JpaRepository<WorkOrderRevisionJpaEntity, Long> {
    Page<WorkOrderRevisionJpaEntity> findByWorkOrderId(Long workOrderId, Pageable pageable);
    Optional<WorkOrderRevisionJpaEntity> findByIdAndWorkOrderId(Long id, Long workOrderId);

    @Query("SELECT COALESCE(MAX(r.revisionNumber),0)+1 FROM WorkOrderRevisionJpaEntity r "
            + "WHERE r.workOrderId=:workOrderId")
    int nextRevisionNumber(@Param("workOrderId") Long workOrderId);

    @Query(value="SELECT COUNT(*) FROM technicians WHERE id_technicians=:id",nativeQuery=true)
    long countTechniciansById(@Param("id") Long id);

    @Query(value="SELECT COUNT(*) FROM users WHERE id_users=:id",nativeQuery=true)
    long countUsersById(@Param("id") Long id);

    @Query(value="SELECT COUNT(*) FROM work_orders wo "
            + "JOIN technicians root_tech ON root_tech.id_technicians=wo.technician_id "
            + "LEFT JOIN work_order_revisions current_revision "
            + "ON current_revision.id_work_order_revisions=wo.current_revision_id "
            + "LEFT JOIN technicians current_tech "
            + "ON current_tech.id_technicians=current_revision.technician_id "
            + "WHERE wo.id_work_orders=:workOrderId "
            + "AND (root_tech.user_id=:userId OR current_tech.user_id=:userId)",nativeQuery=true)
    long countAssignedToTechnicianUser(
            @Param("workOrderId") Long workOrderId, @Param("userId") Long userId);

    @Query(value="SELECT name AS name,description AS description,NULL AS reference "
            + "FROM services WHERE id_services=:id",nativeQuery=true)
    Optional<CatalogSnapshotProjection> findServiceSnapshot(@Param("id") Long id);

    @Query(value="SELECT name AS name,description AS description,NULL AS reference "
            + "FROM parts WHERE id_parts=:id",nativeQuery=true)
    Optional<CatalogSnapshotProjection> findPartSnapshot(@Param("id") Long id);
}
