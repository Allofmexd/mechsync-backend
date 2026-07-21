package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalHistoryQueryPort;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalStatusMapper;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPortalHistoryJdbcAdapter implements CustomerPortalHistoryQueryPort {

    private static final String OWNED_VEHICLES_CTE = """
            WITH owned_vehicles AS (
                SELECT v.id_vehicles AS vehicle_id, v.brand, v.model, v.year
                  FROM vehicles v
                 WHERE v.customer_id = ?
                   AND (? IS NULL OR v.id_vehicles = ?)
            )
            """;
    private static final String EVENTS_SQL = """
            SELECT 'VEHICLE_INTAKE' AS event_type,
                   vi.id_vehicle_intakes AS event_id,
                   ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year) AS vehicle_label,
                   vi.intake_date AS event_date,
                   sc.name AS status_code,
                   vi.id_vehicle_intakes AS intake_id,
                   NULL AS work_order_id,
                   NULL AS job_id
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = vi.status_id
                                    AND sc.context = 'VEHICLE_INTAKES'
            UNION ALL
            SELECT 'WORK_ORDER', wo.id_work_orders, ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year),
                   wo.work_order_date, sc.name, vi.id_vehicle_intakes,
                   wo.id_work_orders,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1)
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN work_orders wo ON wo.vehicle_intake_id = vi.id_vehicle_intakes
              JOIN status_catalog sc ON sc.id_status_catalog = wo.status_id
                                    AND sc.context = 'WORK_ORDERS'
            UNION ALL
            SELECT 'QUOTATION_AVAILABLE', r.id_work_order_revisions, ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year),
                   COALESCE(r.workflow_updated_at, r.created_at), rs.code,
                   vi.id_vehicle_intakes, wo.id_work_orders,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1)
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN work_orders wo ON wo.vehicle_intake_id = vi.id_vehicle_intakes
              JOIN work_order_revisions r
                ON r.id_work_order_revisions = wo.current_revision_id
              JOIN work_order_revision_status_catalog rs
                ON rs.id_work_order_revision_status_catalog = r.revision_status_id
             WHERE rs.code = 'SENT'
            UNION ALL
            SELECT 'QUOTATION_APPROVED', r.id_work_order_revisions, ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year),
                   COALESCE(r.approved_at, r.workflow_updated_at, r.created_at), rs.code,
                   vi.id_vehicle_intakes, wo.id_work_orders,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1)
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN work_orders wo ON wo.vehicle_intake_id = vi.id_vehicle_intakes
              JOIN work_order_revisions r
                ON r.id_work_order_revisions = wo.final_approved_revision_id
              JOIN work_order_revision_status_catalog rs
                ON rs.id_work_order_revision_status_catalog = r.revision_status_id
             WHERE rs.code = 'APPROVED'
            UNION ALL
            SELECT 'JOB_STARTED', j.id_jobs, ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year),
                   j.start_date, sc.name, vi.id_vehicle_intakes,
                   wo.id_work_orders, j.id_jobs
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN work_orders wo ON wo.vehicle_intake_id = vi.id_vehicle_intakes
              JOIN jobs j ON j.work_order_id = wo.id_work_orders
              JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
                                    AND sc.context = 'JOBS'
             WHERE j.start_date IS NOT NULL
            UNION ALL
            SELECT 'JOB_COMPLETED', j.id_jobs, ov.vehicle_id,
                   CONCAT(ov.brand, ' ', ov.model, ' ', ov.year),
                   j.completion_date, sc.name, vi.id_vehicle_intakes,
                   wo.id_work_orders, j.id_jobs
              FROM owned_vehicles ov
              JOIN vehicle_intakes vi ON vi.vehicle_id = ov.vehicle_id
              JOIN work_orders wo ON wo.vehicle_intake_id = vi.id_vehicle_intakes
              JOIN jobs j ON j.work_order_id = wo.id_work_orders
              JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
                                    AND sc.context = 'JOBS'
             WHERE sc.name = 'COMPLETADO' AND j.completion_date IS NOT NULL
            """;
    static final String LIST_SQL = OWNED_VEHICLES_CTE + """
            SELECT * FROM (
            """ + EVENTS_SQL + """
            ) customer_events
             ORDER BY event_date DESC, event_type, event_id DESC
             LIMIT ? OFFSET ?
            """;
    static final String COUNT_SQL = OWNED_VEHICLES_CTE + """
            SELECT COUNT(*) FROM (
            """ + EVENTS_SQL + """
            ) customer_events
            """;

    private final JdbcTemplate jdbc;
    private final CustomerPortalStatusMapper statuses;

    public CustomerPortalHistoryJdbcAdapter(
            JdbcTemplate jdbc, CustomerPortalStatusMapper statuses) {
        this.jdbc = jdbc;
        this.statuses = statuses;
    }

    @Override
    public CustomerPortalPage<CustomerPortalHistoryEvent> findHistory(
            Long customerId, int page, int size, Long vehicleId) {
        long offset = (long) page * size;
        List<CustomerPortalHistoryEvent> content = jdbc.query(
                LIST_SQL,
                (result, row) -> mapEvent(result),
                customerId, vehicleId, vehicleId, size, offset);
        Long count = jdbc.queryForObject(
                COUNT_SQL, Long.class, customerId, vehicleId, vehicleId);
        return CustomerPortalJdbcSupport.page(content, page, size, count);
    }

    private CustomerPortalHistoryEvent mapEvent(ResultSet result) throws SQLException {
        String eventType = result.getString("event_type");
        String visibleStatus = visibleStatus(eventType, result.getString("status_code"));
        return new CustomerPortalHistoryEvent(
                eventType,
                result.getLong("event_id"),
                result.getLong("vehicle_id"),
                result.getString("vehicle_label"),
                CustomerPortalJdbcSupport.dateTime(result, "event_date"),
                title(eventType),
                visibleStatus,
                visibleStatus,
                result.getObject("intake_id", Long.class),
                result.getObject("work_order_id", Long.class),
                result.getObject("job_id", Long.class));
    }

    private String visibleStatus(String eventType, String statusCode) {
        return switch (eventType) {
            case "VEHICLE_INTAKE" -> statuses.intake(statusCode);
            case "WORK_ORDER" -> statuses.workOrder(statusCode);
            case "QUOTATION_AVAILABLE", "QUOTATION_APPROVED" -> statuses.quotation(statusCode);
            case "JOB_STARTED", "JOB_COMPLETED" -> statuses.job(statusCode);
            default -> "Estado en actualización";
        };
    }

    private String title(String eventType) {
        return switch (eventType) {
            case "VEHICLE_INTAKE" -> "Vehículo ingresado";
            case "WORK_ORDER" -> "Orden de trabajo creada";
            case "QUOTATION_AVAILABLE" -> "Cotización disponible";
            case "QUOTATION_APPROVED" -> "Cotización autorizada";
            case "JOB_STARTED" -> "Trabajo iniciado";
            case "JOB_COMPLETED" -> "Trabajo completado";
            default -> "Actualización del servicio";
        };
    }
}
