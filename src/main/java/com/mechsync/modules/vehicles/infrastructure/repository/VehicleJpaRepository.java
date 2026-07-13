package com.mechsync.modules.vehicles.infrastructure.repository;

import com.mechsync.modules.vehicles.infrastructure.persistence.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {

    boolean existsByLicensePlateIgnoreCase(String licensePlate);
    boolean existsByVinIgnoreCase(String vin);
    boolean existsByLicensePlateIgnoreCaseAndIdNot(String licensePlate, Long id);
    boolean existsByVinIgnoreCaseAndIdNot(String vin, Long id);

    @Query(value = "SELECT COUNT(*) FROM customers WHERE id_customers = :customerId", nativeQuery = true)
    long countCustomersById(@Param("customerId") Long customerId);

    @Query(value = "SELECT COUNT(*) FROM vehicle_intakes WHERE vehicle_id = :vehicleId", nativeQuery = true)
    long countIntakesByVehicleId(@Param("vehicleId") Long vehicleId);
}
