package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalJobQueryPort;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalStatusMapper;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobPart;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobService;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPortalJobJdbcAdapter implements CustomerPortalJobQueryPort {

    static final String LIST_SQL = """
            SELECT j.id_jobs AS job_id, j.work_order_id,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   sc.name AS status_code, j.scheduled_start_date, j.start_date, j.completion_date,
                   TRIM(CONCAT_WS(' ', u.first_name, u.last_name)) AS technician_name,
                   sp.name AS specialty_code,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM service_reports sr WHERE sr.job_id = j.id_jobs
                   ) THEN TRUE ELSE FALSE END AS report_available
              FROM jobs j
              JOIN work_orders wo ON wo.id_work_orders = j.work_order_id
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
                                    AND sc.context = 'JOBS'
              JOIN technicians t ON t.id_technicians = j.technician_id
              JOIN users u ON u.id_users = t.user_id
              JOIN specialties sp ON sp.id_specialties = t.specialty_id
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
               AND (? IS NULL OR wo.id_work_orders = ?)
             ORDER BY COALESCE(j.start_date, j.scheduled_start_date, j.created_at) DESC,
                      j.id_jobs DESC
             LIMIT ? OFFSET ?
            """;
    static final String COUNT_SQL = """
            SELECT COUNT(*)
              FROM jobs j
              JOIN work_orders wo ON wo.id_work_orders = j.work_order_id
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
               AND (? IS NULL OR wo.id_work_orders = ?)
            """;
    static final String DETAIL_SQL = """
            SELECT j.id_jobs AS job_id, j.work_order_id,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   sc.name AS status_code, j.scheduled_start_date, j.start_date, j.completion_date,
                   TRIM(CONCAT_WS(' ', u.first_name, u.last_name)) AS technician_name,
                   sp.name AS specialty_code,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM service_reports sr WHERE sr.job_id = j.id_jobs
                   ) THEN TRUE ELSE FALSE END AS report_available
              FROM jobs j
              JOIN work_orders wo ON wo.id_work_orders = j.work_order_id
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
                                    AND sc.context = 'JOBS'
              JOIN technicians t ON t.id_technicians = j.technician_id
              JOIN users u ON u.id_users = t.user_id
              JOIN specialties sp ON sp.id_specialties = t.specialty_id
             WHERE j.id_jobs = ? AND v.customer_id = ?
            """;
    static final String SERVICES_SQL = """
            SELECT s.name AS service_name, js.quantity
              FROM job_services js
              JOIN services s ON s.id_services = js.service_id
             WHERE js.job_id = ?
             ORDER BY js.id_job_services
            """;
    static final String PARTS_SQL = """
            SELECT p.name AS part_name, jp.quantity_used
              FROM job_parts jp
              JOIN parts p ON p.id_parts = jp.part_id
             WHERE jp.job_id = ?
             ORDER BY jp.id_job_parts
            """;

    private final JdbcTemplate jdbc;
    private final CustomerPortalStatusMapper statuses;

    public CustomerPortalJobJdbcAdapter(
            JdbcTemplate jdbc, CustomerPortalStatusMapper statuses) {
        this.jdbc = jdbc;
        this.statuses = statuses;
    }

    @Override
    public CustomerPortalPage<CustomerPortalJobSummary> findJobs(
            Long customerId, int page, int size, Long vehicleId, Long workOrderId) {
        long offset = (long) page * size;
        List<CustomerPortalJobSummary> content = jdbc.query(
                LIST_SQL,
                (result, row) -> new CustomerPortalJobSummary(
                        result.getLong("job_id"),
                        result.getLong("work_order_id"),
                        CustomerPortalJdbcSupport.vehicle(result),
                        statuses.job(result.getString("status_code")),
                        CustomerPortalJdbcSupport.dateTime(result, "scheduled_start_date"),
                        CustomerPortalJdbcSupport.dateTime(result, "start_date"),
                        CustomerPortalJdbcSupport.dateTime(result, "completion_date"),
                        result.getString("technician_name"),
                        statuses.specialty(result.getString("specialty_code")),
                        result.getBoolean("report_available")),
                customerId,
                vehicleId, vehicleId,
                workOrderId, workOrderId,
                size, offset);
        Long count = jdbc.queryForObject(
                COUNT_SQL,
                Long.class,
                customerId,
                vehicleId, vehicleId,
                workOrderId, workOrderId);
        return CustomerPortalJdbcSupport.page(content, page, size, count);
    }

    @Override
    public Optional<CustomerPortalJobDetail> findJob(Long customerId, Long jobId) {
        Optional<JobHeader> header = jdbc.query(
                        DETAIL_SQL,
                        (result, row) -> new JobHeader(
                                result.getLong("job_id"),
                                result.getLong("work_order_id"),
                                CustomerPortalJdbcSupport.vehicle(result),
                                result.getString("status_code"),
                                CustomerPortalJdbcSupport.dateTime(result, "scheduled_start_date"),
                                CustomerPortalJdbcSupport.dateTime(result, "start_date"),
                                CustomerPortalJdbcSupport.dateTime(result, "completion_date"),
                                result.getString("technician_name"),
                                result.getString("specialty_code"),
                                result.getBoolean("report_available")),
                        jobId, customerId)
                .stream()
                .findFirst();
        return header.map(this::assembleJob);
    }

    private CustomerPortalJobDetail assembleJob(JobHeader header) {
        List<CustomerPortalJobService> services = jdbc.query(
                SERVICES_SQL,
                (result, row) -> new CustomerPortalJobService(
                        result.getString("service_name"), result.getBigDecimal("quantity")),
                header.jobId());
        List<CustomerPortalJobPart> parts = jdbc.query(
                PARTS_SQL,
                (result, row) -> new CustomerPortalJobPart(
                        result.getString("part_name"), result.getBigDecimal("quantity_used")),
                header.jobId());
        return new CustomerPortalJobDetail(
                header.jobId(), header.workOrderId(), header.vehicle(),
                statuses.job(header.statusCode()), header.scheduledStartDate(),
                header.actualStartDate(), header.actualEndDate(), header.technicianName(),
                statuses.specialty(header.specialtyCode()), header.workOrderId(),
                header.reportAvailable(), services, parts);
    }

    private record JobHeader(
            Long jobId,
            Long workOrderId,
            com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleInfo vehicle,
            String statusCode,
            LocalDateTime scheduledStartDate,
            LocalDateTime actualStartDate,
            LocalDateTime actualEndDate,
            String technicianName,
            String specialtyCode,
            boolean reportAvailable) {
    }
}
