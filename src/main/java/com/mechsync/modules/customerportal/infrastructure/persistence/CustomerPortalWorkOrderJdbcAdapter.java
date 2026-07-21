package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalWorkOrderQueryPort;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalStatusMapper;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotationPartLine;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotationServiceLine;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPortalWorkOrderJdbcAdapter implements CustomerPortalWorkOrderQueryPort {

    private static final String VISIBLE_QUOTATION_EXISTS = """
            EXISTS (
                SELECT 1
                  FROM work_order_revisions r
                  JOIN work_order_revision_status_catalog rs
                    ON rs.id_work_order_revision_status_catalog = r.revision_status_id
                 WHERE r.work_order_id = wo.id_work_orders
                   AND ((r.id_work_order_revisions = wo.final_approved_revision_id
                         AND rs.code = 'APPROVED')
                     OR (r.id_work_order_revisions = wo.current_revision_id
                         AND rs.code = 'SENT'))
            )
            """;
    static final String LIST_SQL = """
            SELECT wo.id_work_orders AS work_order_id,
                   vi.id_vehicle_intakes AS intake_id,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   wo.work_order_date, sc.name AS status_code,
                   wo.estimated_start_date, wo.estimated_delivery_date, wo.estimated_hours,
                   CASE WHEN %s THEN TRUE ELSE FALSE END AS quotation_available,
                   (SELECT rs.code
                      FROM work_order_revisions r
                      JOIN work_order_revision_status_catalog rs
                        ON rs.id_work_order_revision_status_catalog = r.revision_status_id
                     WHERE r.work_order_id = wo.id_work_orders
                       AND ((r.id_work_order_revisions = wo.final_approved_revision_id AND rs.code = 'APPROVED')
                         OR (r.id_work_order_revisions = wo.current_revision_id AND rs.code = 'SENT'))
                     ORDER BY CASE WHEN r.id_work_order_revisions = wo.final_approved_revision_id THEN 0 ELSE 1 END
                     LIMIT 1) AS quotation_status,
                   (SELECT r.total_amount
                      FROM work_order_revisions r
                      JOIN work_order_revision_status_catalog rs
                        ON rs.id_work_order_revision_status_catalog = r.revision_status_id
                     WHERE r.work_order_id = wo.id_work_orders
                       AND ((r.id_work_order_revisions = wo.final_approved_revision_id AND rs.code = 'APPROVED')
                         OR (r.id_work_order_revisions = wo.current_revision_id AND rs.code = 'SENT'))
                     ORDER BY CASE WHEN r.id_work_order_revisions = wo.final_approved_revision_id THEN 0 ELSE 1 END
                     LIMIT 1) AS quotation_total,
                   (SELECT r.currency
                      FROM work_order_revisions r
                      JOIN work_order_revision_status_catalog rs
                        ON rs.id_work_order_revision_status_catalog = r.revision_status_id
                     WHERE r.work_order_id = wo.id_work_orders
                       AND ((r.id_work_order_revisions = wo.final_approved_revision_id AND rs.code = 'APPROVED')
                         OR (r.id_work_order_revisions = wo.current_revision_id AND rs.code = 'SENT'))
                     ORDER BY CASE WHEN r.id_work_order_revisions = wo.final_approved_revision_id THEN 0 ELSE 1 END
                     LIMIT 1) AS quotation_currency,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1) AS job_id
              FROM work_orders wo
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = wo.status_id
                                    AND sc.context = 'WORK_ORDERS'
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
               AND (? IS NULL OR vi.id_vehicle_intakes = ?)
               AND (? = FALSE OR %s)
             ORDER BY wo.work_order_date DESC, wo.id_work_orders DESC
             LIMIT ? OFFSET ?
            """.formatted(VISIBLE_QUOTATION_EXISTS, VISIBLE_QUOTATION_EXISTS);
    static final String COUNT_SQL = """
            SELECT COUNT(*)
              FROM work_orders wo
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
               AND (? IS NULL OR vi.id_vehicle_intakes = ?)
               AND (? = FALSE OR %s)
            """.formatted(VISIBLE_QUOTATION_EXISTS);
    static final String DETAIL_SQL = """
            SELECT wo.id_work_orders AS work_order_id,
                   vi.id_vehicle_intakes AS intake_id, vi.intake_date, vi.reported_problem,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   wo.work_order_date, sc.name AS status_code,
                   wo.estimated_start_date, wo.estimated_delivery_date, wo.estimated_hours,
                   CASE WHEN %s THEN TRUE ELSE FALSE END AS quotation_available,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1) AS job_id
              FROM work_orders wo
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = wo.status_id
                                    AND sc.context = 'WORK_ORDERS'
             WHERE wo.id_work_orders = ? AND v.customer_id = ?
            """.formatted(VISIBLE_QUOTATION_EXISTS);
    static final String OWNS_SQL = """
            SELECT COUNT(*)
              FROM work_orders wo
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
             WHERE wo.id_work_orders = ? AND v.customer_id = ?
            """;
    static final String QUOTATION_SQL = """
            SELECT r.id_work_order_revisions AS revision_id, r.work_order_id,
                   r.revision_number, rs.code AS status_code, r.currency,
                   r.subtotal_amount, r.apply_iva, r.iva_rate, r.iva_amount, r.total_amount,
                   r.estimated_start_date, r.estimated_delivery_date, r.estimated_hours,
                   r.customer_notes, r.accepted_at, am.name AS acceptance_method
              FROM work_orders wo
              JOIN vehicle_intakes vi ON vi.id_vehicle_intakes = wo.vehicle_intake_id
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN work_order_revisions r ON r.work_order_id = wo.id_work_orders
              JOIN work_order_revision_status_catalog rs
                ON rs.id_work_order_revision_status_catalog = r.revision_status_id
              LEFT JOIN work_order_acceptance_method_catalog am
                ON am.id_work_order_acceptance_method_catalog = r.acceptance_method_id
             WHERE wo.id_work_orders = ? AND v.customer_id = ?
               AND ((r.id_work_order_revisions = wo.final_approved_revision_id
                     AND rs.code = 'APPROVED')
                 OR (r.id_work_order_revisions = wo.current_revision_id
                     AND rs.code = 'SENT'))
             ORDER BY CASE
                 WHEN r.id_work_order_revisions = wo.final_approved_revision_id
                      AND rs.code = 'APPROVED' THEN 0 ELSE 1 END
             LIMIT 1
            """;
    static final String QUOTATION_SERVICES_SQL = """
            SELECT service_name_snapshot, service_description_snapshot,
                   quantity, unit_price_snapshot, line_total_snapshot
              FROM work_order_revision_services
             WHERE work_order_revision_id = ?
             ORDER BY line_number, id_work_order_revision_services
            """;
    static final String QUOTATION_PARTS_SQL = """
            SELECT part_name_snapshot, part_number_snapshot, part_description_snapshot,
                   quantity, unit_price_snapshot, line_total_snapshot
              FROM work_order_revision_parts
             WHERE work_order_revision_id = ?
             ORDER BY line_number, id_work_order_revision_parts
            """;

    private final JdbcTemplate jdbc;
    private final CustomerPortalStatusMapper statuses;

    public CustomerPortalWorkOrderJdbcAdapter(
            JdbcTemplate jdbc, CustomerPortalStatusMapper statuses) {
        this.jdbc = jdbc;
        this.statuses = statuses;
    }

    @Override
    public CustomerPortalPage<CustomerPortalWorkOrderSummary> findWorkOrders(
            Long customerId, int page, int size, Long vehicleId, Long intakeId,
            boolean quotationOnly) {
        long offset = (long) page * size;
        List<CustomerPortalWorkOrderSummary> content = jdbc.query(
                LIST_SQL,
                (result, row) -> new CustomerPortalWorkOrderSummary(
                        result.getLong("work_order_id"),
                        result.getLong("intake_id"),
                        CustomerPortalJdbcSupport.vehicle(result),
                        CustomerPortalJdbcSupport.dateTime(result, "work_order_date"),
                        statuses.workOrder(result.getString("status_code")),
                        CustomerPortalJdbcSupport.dateTime(result, "estimated_start_date"),
                        CustomerPortalJdbcSupport.dateTime(result, "estimated_delivery_date"),
                        result.getBigDecimal("estimated_hours"),
                        result.getBoolean("quotation_available"),
                        result.getString("quotation_status") == null
                                ? null
                                : statuses.quotation(result.getString("quotation_status")),
                        result.getBigDecimal("quotation_total"),
                        result.getString("quotation_currency"),
                        result.getObject("job_id", Long.class)),
                customerId,
                vehicleId, vehicleId,
                intakeId, intakeId,
                quotationOnly,
                size, offset);
        Long count = jdbc.queryForObject(
                COUNT_SQL,
                Long.class,
                customerId,
                vehicleId, vehicleId,
                intakeId, intakeId,
                quotationOnly);
        return CustomerPortalJdbcSupport.page(content, page, size, count);
    }

    @Override
    public Optional<CustomerPortalWorkOrderDetail> findWorkOrder(
            Long customerId, Long workOrderId) {
        return jdbc.query(
                        DETAIL_SQL,
                        (result, row) -> new CustomerPortalWorkOrderDetail(
                                result.getLong("work_order_id"),
                                result.getLong("intake_id"),
                                CustomerPortalJdbcSupport.vehicle(result),
                                CustomerPortalJdbcSupport.dateTime(result, "intake_date"),
                                result.getString("reported_problem"),
                                CustomerPortalJdbcSupport.dateTime(result, "work_order_date"),
                                statuses.workOrder(result.getString("status_code")),
                                CustomerPortalJdbcSupport.dateTime(result, "estimated_start_date"),
                                CustomerPortalJdbcSupport.dateTime(result, "estimated_delivery_date"),
                                result.getBigDecimal("estimated_hours"),
                                result.getBoolean("quotation_available"),
                                result.getObject("job_id", Long.class)),
                        workOrderId, customerId)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<CustomerPortalQuotation> findVisibleQuotation(
            Long customerId, Long workOrderId) {
        Optional<QuotationHeader> header = jdbc.query(
                        QUOTATION_SQL,
                        (result, row) -> new QuotationHeader(
                                result.getLong("work_order_id"),
                                result.getLong("revision_id"),
                                result.getInt("revision_number"),
                                result.getString("status_code"),
                                result.getString("currency"),
                                result.getBigDecimal("subtotal_amount"),
                                result.getBoolean("apply_iva"),
                                result.getBigDecimal("iva_rate"),
                                result.getBigDecimal("iva_amount"),
                                result.getBigDecimal("total_amount"),
                                CustomerPortalJdbcSupport.dateTime(result, "estimated_start_date"),
                                CustomerPortalJdbcSupport.dateTime(result, "estimated_delivery_date"),
                                result.getBigDecimal("estimated_hours"),
                                result.getString("customer_notes"),
                                CustomerPortalJdbcSupport.dateTime(result, "accepted_at"),
                                result.getString("acceptance_method")),
                        workOrderId, customerId)
                .stream()
                .findFirst();
        return header.map(this::assembleQuotation);
    }

    @Override
    public boolean ownsWorkOrder(Long customerId, Long workOrderId) {
        Long count = jdbc.queryForObject(OWNS_SQL, Long.class, workOrderId, customerId);
        return count != null && count > 0;
    }

    private CustomerPortalQuotation assembleQuotation(QuotationHeader header) {
        List<CustomerPortalQuotationServiceLine> services = jdbc.query(
                QUOTATION_SERVICES_SQL,
                (result, row) -> new CustomerPortalQuotationServiceLine(
                        result.getString("service_name_snapshot"),
                        result.getString("service_description_snapshot"),
                        result.getBigDecimal("quantity"),
                        result.getBigDecimal("unit_price_snapshot"),
                        result.getBigDecimal("line_total_snapshot")),
                header.revisionId());
        List<CustomerPortalQuotationPartLine> parts = jdbc.query(
                QUOTATION_PARTS_SQL,
                (result, row) -> new CustomerPortalQuotationPartLine(
                        result.getString("part_name_snapshot"),
                        result.getString("part_number_snapshot"),
                        result.getString("part_description_snapshot"),
                        result.getBigDecimal("quantity"),
                        result.getBigDecimal("unit_price_snapshot"),
                        result.getBigDecimal("line_total_snapshot")),
                header.revisionId());
        return new CustomerPortalQuotation(
                header.workOrderId(), header.revisionId(), header.revisionNumber(),
                statuses.quotation(header.statusCode()), header.currency(),
                header.subtotal(), header.applyIva(), header.ivaRate(), header.ivaAmount(),
                header.total(), header.estimatedStartDate(), header.estimatedDeliveryDate(),
                header.estimatedHours(), header.customerNotes(), header.acceptedAt(),
                header.acceptanceMethod(), services, parts);
    }

    private record QuotationHeader(
            Long workOrderId,
            Long revisionId,
            Integer revisionNumber,
            String statusCode,
            String currency,
            BigDecimal subtotal,
            boolean applyIva,
            BigDecimal ivaRate,
            BigDecimal ivaAmount,
            BigDecimal total,
            LocalDateTime estimatedStartDate,
            LocalDateTime estimatedDeliveryDate,
            BigDecimal estimatedHours,
            String customerNotes,
            LocalDateTime acceptedAt,
            String acceptanceMethod) {
    }
}
