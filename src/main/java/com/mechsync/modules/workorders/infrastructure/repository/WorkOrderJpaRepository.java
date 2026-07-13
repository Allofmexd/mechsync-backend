package com.mechsync.modules.workorders.infrastructure.repository;
import com.mechsync.modules.workorders.infrastructure.persistence.WorkOrderJpaEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrderJpaEntity,Long> {
 @Query(value="SELECT COUNT(*) FROM vehicle_intakes WHERE id_vehicle_intakes=:id",nativeQuery=true)
 long countVehicleIntakesById(@Param("id") Long id);
 @Query(value="SELECT COUNT(*) FROM technicians WHERE id_technicians=:id",nativeQuery=true)
 long countTechniciansById(@Param("id") Long id);
 @Query(value="SELECT COUNT(*) FROM status_catalog WHERE id_status_catalog=:id AND context='WORK_ORDERS'",nativeQuery=true)
 long countWorkOrderStatusesById(@Param("id") Long id);
 @Query(value="SELECT (SELECT COUNT(*) FROM jobs WHERE work_order_id=:id) + "
   +"(SELECT COUNT(*) FROM work_order_services WHERE work_order_id=:id) + "
   +"(SELECT COUNT(*) FROM work_order_parts WHERE work_order_id=:id)",nativeQuery=true)
 long countDependenciesByWorkOrderId(@Param("id") Long id);
}
