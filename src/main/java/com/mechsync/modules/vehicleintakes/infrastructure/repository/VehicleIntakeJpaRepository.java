package com.mechsync.modules.vehicleintakes.infrastructure.repository;

import com.mechsync.modules.vehicleintakes.infrastructure.persistence.VehicleIntakeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleIntakeJpaRepository extends JpaRepository<VehicleIntakeJpaEntity, Long> {
    @Query(value = "SELECT COUNT(*) FROM vehicles WHERE id_vehicles = :id", nativeQuery = true)
    long countVehiclesById(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM technicians WHERE id_technicians = :id", nativeQuery = true)
    long countTechniciansById(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM status_catalog "
            + "WHERE id_status_catalog = :id AND context = 'VEHICLE_INTAKES'", nativeQuery = true)
    long countIntakeStatusesById(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM work_orders WHERE vehicle_intake_id = :id", nativeQuery = true)
    long countWorkOrdersByIntakeId(@Param("id") Long id);
}
