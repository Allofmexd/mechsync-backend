package com.mechsync.modules.servicereports.infrastructure.repository;

import com.mechsync.modules.servicereports.infrastructure.persistence.ServiceReportJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceReportJpaRepository extends JpaRepository<ServiceReportJpaEntity, Long> {
    boolean existsByJobId(Long jobId);
    Optional<ServiceReportJpaEntity> findByJobId(Long jobId);

    @Query(value = """
            SELECT sr.* FROM service_reports sr
            INNER JOIN jobs j ON j.id_jobs = sr.job_id
            WHERE j.technician_id = :technicianId
            ORDER BY sr.id_service_reports DESC
            """, countQuery = """
            SELECT COUNT(*) FROM service_reports sr
            INNER JOIN jobs j ON j.id_jobs = sr.job_id
            WHERE j.technician_id = :technicianId
            """, nativeQuery = true)
    Page<ServiceReportJpaEntity> findAllByTechnicianId(
            @Param("technicianId") Long technicianId, Pageable pageable);

    @Query(value = """
            SELECT sr.* FROM service_reports sr
            INNER JOIN jobs j ON j.id_jobs = sr.job_id
            WHERE sr.id_service_reports = :reportId
              AND j.technician_id = :technicianId
            """, nativeQuery = true)
    Optional<ServiceReportJpaEntity> findByIdAndTechnicianId(
            @Param("reportId") Long reportId,
            @Param("technicianId") Long technicianId);

    @Query(value = """
            SELECT sr.* FROM service_reports sr
            INNER JOIN jobs j ON j.id_jobs = sr.job_id
            WHERE sr.job_id = :jobId
              AND j.technician_id = :technicianId
            """, nativeQuery = true)
    Optional<ServiceReportJpaEntity> findByJobIdAndTechnicianId(
            @Param("jobId") Long jobId,
            @Param("technicianId") Long technicianId);

    @Query(value = """
            SELECT j.id_jobs AS jobId, sc.name AS statusCode,
                   j.actual_subtotal AS actualSubtotal, j.actual_iva AS actualIva,
                   j.actual_total AS actualTotal
            FROM jobs j
            JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
            WHERE j.id_jobs = :jobId AND sc.context = 'JOBS'
            """, nativeQuery = true)
    Optional<JobClosureView> findJobClosure(@Param("jobId") Long jobId);

    @Query(value = """
            SELECT sr.id_service_reports AS reportId, sr.job_id AS jobId,
                   report_status.name AS reportStatus, sr.report_date AS reportDate,
                   sr.final_description AS finalDescription,
                   sr.final_subtotal AS finalSubtotal, sr.final_iva AS finalIva,
                   sr.final_total AS finalTotal,
                   sr.customer_confirmation AS customerConfirmation,
                   sr.delivered_at AS deliveredAt,
                   j.work_order_id AS workOrderId,
                   wo.vehicle_intake_id AS vehicleIntakeId,
                   j.technician_id AS technicianId,
                   CONCAT_WS(' ', technician_user.first_name,
                                   technician_user.last_name) AS technicianName,
                   vehicle.customer_id AS customerId,
                   CONCAT_WS(' ', customer_user.first_name,
                                   customer_user.last_name) AS customerName,
                   intake.vehicle_id AS vehicleId,
                   vehicle.brand AS vehicleBrand, vehicle.model AS vehicleModel,
                   vehicle.year AS vehicleYear, vehicle.license_plate AS licensePlate,
                   vehicle.vin AS vin,
                   COALESCE(intake.intake_mileage, vehicle.current_mileage) AS mileage
            FROM service_reports sr
            JOIN status_catalog report_status
              ON report_status.id_status_catalog = sr.status_id
             AND report_status.context = 'SERVICE_REPORTS'
            JOIN jobs j ON j.id_jobs = sr.job_id
            JOIN work_orders wo ON wo.id_work_orders = j.work_order_id
            JOIN vehicle_intakes intake
              ON intake.id_vehicle_intakes = wo.vehicle_intake_id
            JOIN vehicles vehicle ON vehicle.id_vehicles = intake.vehicle_id
            JOIN customers customer ON customer.id_customers = vehicle.customer_id
            JOIN users customer_user ON customer_user.id_users = customer.user_id
            JOIN technicians technician ON technician.id_technicians = j.technician_id
            JOIN users technician_user ON technician_user.id_users = technician.user_id
            WHERE sr.id_service_reports = :reportId
            """, nativeQuery = true)
    Optional<ServiceReportPdfHeaderView> findPdfHeader(@Param("reportId") Long reportId);

    @Query(value = """
            SELECT sr.id_service_reports AS reportId, sr.job_id AS jobId,
                   report_status.name AS reportStatus, sr.report_date AS reportDate,
                   sr.final_description AS finalDescription,
                   sr.final_subtotal AS finalSubtotal, sr.final_iva AS finalIva,
                   sr.final_total AS finalTotal,
                   sr.customer_confirmation AS customerConfirmation,
                   sr.delivered_at AS deliveredAt,
                   j.work_order_id AS workOrderId,
                   wo.vehicle_intake_id AS vehicleIntakeId,
                   j.technician_id AS technicianId,
                   CONCAT_WS(' ', technician_user.first_name,
                                   technician_user.last_name) AS technicianName,
                   vehicle.customer_id AS customerId,
                   CONCAT_WS(' ', customer_user.first_name,
                                   customer_user.last_name) AS customerName,
                   intake.vehicle_id AS vehicleId,
                   vehicle.brand AS vehicleBrand, vehicle.model AS vehicleModel,
                   vehicle.year AS vehicleYear, vehicle.license_plate AS licensePlate,
                   vehicle.vin AS vin,
                   COALESCE(intake.intake_mileage, vehicle.current_mileage) AS mileage
            FROM service_reports sr
            JOIN status_catalog report_status
              ON report_status.id_status_catalog = sr.status_id
             AND report_status.context = 'SERVICE_REPORTS'
            JOIN jobs j ON j.id_jobs = sr.job_id
            JOIN work_orders wo ON wo.id_work_orders = j.work_order_id
            JOIN vehicle_intakes intake
              ON intake.id_vehicle_intakes = wo.vehicle_intake_id
            JOIN vehicles vehicle ON vehicle.id_vehicles = intake.vehicle_id
            JOIN customers customer ON customer.id_customers = vehicle.customer_id
            JOIN users customer_user ON customer_user.id_users = customer.user_id
            JOIN technicians technician ON technician.id_technicians = j.technician_id
            JOIN users technician_user ON technician_user.id_users = technician.user_id
            WHERE sr.id_service_reports = :reportId
              AND j.technician_id = :technicianId
            """, nativeQuery = true)
    Optional<ServiceReportPdfHeaderView> findPdfHeaderAssignedToTechnician(
            @Param("reportId") Long reportId,
            @Param("technicianId") Long technicianId);

    @Query(value = """
            SELECT catalog.name AS name, line.quantity AS quantity,
                   line.actual_unit_price AS unitPrice,
                   line.actual_subtotal AS subtotal
            FROM service_reports sr
            JOIN job_services line ON line.job_id = sr.job_id
            JOIN services catalog ON catalog.id_services = line.service_id
            WHERE sr.id_service_reports = :reportId
            ORDER BY line.id_job_services
            """, nativeQuery = true)
    List<ServiceReportPdfServiceLineView> findPdfServices(@Param("reportId") Long reportId);

    @Query(value = """
            SELECT catalog.name AS name, line.quantity_used AS quantity,
                   unit.name AS measurementUnit,
                   line.actual_unit_price AS unitPrice,
                   line.actual_subtotal AS subtotal
            FROM service_reports sr
            JOIN job_parts line ON line.job_id = sr.job_id
            JOIN parts catalog ON catalog.id_parts = line.part_id
            JOIN measurement_units unit ON unit.id_measurement_units = catalog.unit_id
            WHERE sr.id_service_reports = :reportId
            ORDER BY line.id_job_parts
            """, nativeQuery = true)
    List<ServiceReportPdfPartLineView> findPdfParts(@Param("reportId") Long reportId);
}
