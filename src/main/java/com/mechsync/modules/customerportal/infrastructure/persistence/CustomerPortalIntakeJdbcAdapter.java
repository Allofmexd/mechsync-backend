package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalIntakeQueryPort;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalStatusMapper;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderLink;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPortalIntakeJdbcAdapter implements CustomerPortalIntakeQueryPort {

    static final String LIST_SQL = """
            SELECT vi.id_vehicle_intakes AS intake_id,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   vi.intake_date, vi.intake_mileage, vi.reported_problem,
                   sc.name AS status_code, vi.updated_at
              FROM vehicle_intakes vi
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = vi.status_id
                                    AND sc.context = 'VEHICLE_INTAKES'
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
             ORDER BY vi.intake_date DESC, vi.id_vehicle_intakes DESC
             LIMIT ? OFFSET ?
            """;
    static final String COUNT_SQL = """
            SELECT COUNT(*)
              FROM vehicle_intakes vi
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
             WHERE v.customer_id = ?
               AND (? IS NULL OR v.id_vehicles = ?)
            """;
    static final String DETAIL_SQL = """
            SELECT vi.id_vehicle_intakes AS intake_id,
                   v.id_vehicles AS vehicle_id, v.brand, v.model, v.year, v.license_plate,
                   vi.intake_date, vi.intake_mileage, vi.reported_problem,
                   sc.name AS status_code, vi.updated_at
              FROM vehicle_intakes vi
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
              JOIN status_catalog sc ON sc.id_status_catalog = vi.status_id
                                    AND sc.context = 'VEHICLE_INTAKES'
             WHERE vi.id_vehicle_intakes = ?
               AND v.customer_id = ?
            """;
    static final String WORK_ORDER_LINKS_SQL = """
            SELECT wo.id_work_orders AS work_order_id, wo.work_order_date,
                   sc.name AS status_code,
                   CASE WHEN EXISTS (
                       SELECT 1
                         FROM work_order_revisions r
                         JOIN work_order_revision_status_catalog rs
                           ON rs.id_work_order_revision_status_catalog = r.revision_status_id
                        WHERE r.work_order_id = wo.id_work_orders
                          AND ((r.id_work_order_revisions = wo.final_approved_revision_id
                                AND rs.code = 'APPROVED')
                            OR (r.id_work_order_revisions = wo.current_revision_id
                                AND rs.code = 'SENT'))
                   ) THEN TRUE ELSE FALSE END AS quotation_available,
                   (SELECT j.id_jobs FROM jobs j
                     WHERE j.work_order_id = wo.id_work_orders LIMIT 1) AS job_id
              FROM work_orders wo
              JOIN status_catalog sc ON sc.id_status_catalog = wo.status_id
                                    AND sc.context = 'WORK_ORDERS'
             WHERE wo.vehicle_intake_id = ?
             ORDER BY wo.work_order_date DESC, wo.id_work_orders DESC
            """;
    static final String OWNS_SQL = """
            SELECT COUNT(*)
              FROM vehicle_intakes vi
              JOIN vehicles v ON v.id_vehicles = vi.vehicle_id
             WHERE vi.id_vehicle_intakes = ? AND v.customer_id = ?
            """;

    private final JdbcTemplate jdbc;
    private final CustomerPortalStatusMapper statuses;

    public CustomerPortalIntakeJdbcAdapter(
            JdbcTemplate jdbc, CustomerPortalStatusMapper statuses) {
        this.jdbc = jdbc;
        this.statuses = statuses;
    }

    @Override
    public CustomerPortalPage<CustomerPortalIntakeSummary> findIntakes(
            Long customerId, int page, int size, Long vehicleId) {
        long offset = (long) page * size;
        List<CustomerPortalIntakeSummary> content = jdbc.query(
                LIST_SQL,
                (result, row) -> new CustomerPortalIntakeSummary(
                        result.getLong("intake_id"),
                        result.getLong("vehicle_id"),
                        CustomerPortalJdbcSupport.vehicle(result).label(),
                        CustomerPortalJdbcSupport.dateTime(result, "intake_date"),
                        result.getObject("intake_mileage", Integer.class),
                        result.getString("reported_problem"),
                        statuses.intake(result.getString("status_code")),
                        CustomerPortalJdbcSupport.dateTime(result, "updated_at")),
                customerId, vehicleId, vehicleId, size, offset);
        Long count = jdbc.queryForObject(COUNT_SQL, Long.class, customerId, vehicleId, vehicleId);
        return CustomerPortalJdbcSupport.page(content, page, size, count);
    }

    @Override
    public Optional<CustomerPortalIntakeDetail> findIntake(Long customerId, Long intakeId) {
        Optional<CustomerPortalIntakeDetail> detail = jdbc.query(
                        DETAIL_SQL,
                        (result, row) -> new CustomerPortalIntakeDetail(
                                result.getLong("intake_id"),
                                CustomerPortalJdbcSupport.vehicle(result),
                                CustomerPortalJdbcSupport.dateTime(result, "intake_date"),
                                result.getObject("intake_mileage", Integer.class),
                                result.getString("reported_problem"),
                                statuses.intake(result.getString("status_code")),
                                CustomerPortalJdbcSupport.dateTime(result, "updated_at"),
                                List.of()),
                        intakeId, customerId)
                .stream()
                .findFirst();
        return detail.map(value -> new CustomerPortalIntakeDetail(
                value.intakeId(), value.vehicle(), value.intakeDate(), value.intakeMileage(),
                value.reportedProblem(), value.visibleStatus(), value.updatedAt(),
                findWorkOrderLinks(intakeId)));
    }

    @Override
    public boolean ownsIntake(Long customerId, Long intakeId) {
        Long count = jdbc.queryForObject(OWNS_SQL, Long.class, intakeId, customerId);
        return count != null && count > 0;
    }

    private List<CustomerPortalWorkOrderLink> findWorkOrderLinks(Long intakeId) {
        return jdbc.query(
                WORK_ORDER_LINKS_SQL,
                (result, row) -> new CustomerPortalWorkOrderLink(
                        result.getLong("work_order_id"),
                        CustomerPortalJdbcSupport.dateTime(result, "work_order_date"),
                        statuses.workOrder(result.getString("status_code")),
                        result.getBoolean("quotation_available"),
                        result.getObject("job_id", Long.class)),
                intakeId);
    }
}
