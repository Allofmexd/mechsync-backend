package com.mechsync.modules.dashboard.infrastructure.persistence;

import com.mechsync.modules.dashboard.application.port.out.DashboardMetricsPort;
import com.mechsync.modules.dashboard.domain.model.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardJdbcAdapter implements DashboardMetricsPort {
    static final String SUMMARY_SQL = """
            SELECT
              (SELECT COUNT(*) FROM customers) AS registered_customers,
              (SELECT COUNT(*) FROM vehicles) AS registered_vehicles,
              (SELECT COUNT(*)
                 FROM vehicle_intakes intake
                 JOIN status_catalog status
                   ON status.id_status_catalog = intake.status_id
                  AND status.context = 'VEHICLE_INTAKES'
                WHERE status.name NOT IN ('COMPLETADO', 'CANCELADO')) AS open_vehicle_intakes,
              (SELECT COUNT(*)
                 FROM work_orders work_order
                 JOIN status_catalog status
                   ON status.id_status_catalog = work_order.status_id
                  AND status.context = 'WORK_ORDERS'
                WHERE status.name IN ('PENDIENTE', 'APROBADO', 'EN_PROCESO')) AS active_work_orders,
              (SELECT COUNT(*)
                 FROM jobs job
                 JOIN status_catalog status
                   ON status.id_status_catalog = job.status_id
                  AND status.context = 'JOBS'
                WHERE status.name = 'EN_PROCESO') AS jobs_in_progress,
              (SELECT COALESCE(SUM(report.final_total), 0.00)
                 FROM service_reports report
                 JOIN status_catalog status
                   ON status.id_status_catalog = report.status_id
                  AND status.context = 'SERVICE_REPORTS'
                WHERE status.name = 'ENTREGADO'
                  AND report.delivered_at >= ?
                  AND report.delivered_at < ?) AS period_revenue
            """;

    static final String WORK_ORDERS_BY_STATUS_SQL = """
            SELECT status.name AS status_code, COUNT(work_order.id_work_orders) AS metric_count
              FROM status_catalog status
              LEFT JOIN work_orders work_order
                ON work_order.status_id = status.id_status_catalog
               AND work_order.work_order_date >= ?
               AND work_order.work_order_date < ?
             WHERE status.context = 'WORK_ORDERS'
             GROUP BY status.id_status_catalog, status.name
             ORDER BY status.id_status_catalog
            """;

    static final String JOBS_BY_STATUS_SQL = """
            SELECT status.name AS status_code, COUNT(job.id_jobs) AS metric_count
              FROM status_catalog status
              LEFT JOIN jobs job
                ON job.status_id = status.id_status_catalog
               AND job.created_at >= ?
               AND job.created_at < ?
             WHERE status.context = 'JOBS'
             GROUP BY status.id_status_catalog, status.name
             ORDER BY status.id_status_catalog
            """;

    static final String REVENUE_BY_MONTH_SQL = """
            SELECT DATE_FORMAT(report.delivered_at, '%Y-%m') AS period_code,
                   YEAR(report.delivered_at) AS period_year,
                   MONTH(report.delivered_at) AS period_month,
                   SUM(report.final_total) AS period_total
              FROM service_reports report
              JOIN status_catalog status
                ON status.id_status_catalog = report.status_id
               AND status.context = 'SERVICE_REPORTS'
             WHERE status.name = 'ENTREGADO'
               AND report.delivered_at >= ?
               AND report.delivered_at < ?
             GROUP BY YEAR(report.delivered_at), MONTH(report.delivered_at),
                      DATE_FORMAT(report.delivered_at, '%Y-%m')
             ORDER BY period_year, period_month
            """;

    static final String TOP_SERVICES_SQL = """
            SELECT service.id_services AS service_id, service.name AS service_name,
                   SUM(line.quantity) AS performed_quantity
              FROM job_services line
              JOIN services service ON service.id_services = line.service_id
              JOIN jobs job ON job.id_jobs = line.job_id
              JOIN status_catalog status
                ON status.id_status_catalog = job.status_id
               AND status.context = 'JOBS'
             WHERE status.name = 'COMPLETADO'
               AND job.completion_date >= ?
               AND job.completion_date < ?
             GROUP BY service.id_services, service.name
             ORDER BY performed_quantity DESC, service.id_services
             LIMIT ?
            """;

    static final String TECHNICIAN_WORKLOAD_SQL = """
            SELECT technician.id_technicians AS technician_id,
                   CONCAT_WS(' ', user.first_name, user.last_name) AS technician_name,
                   COUNT(job.id_jobs) AS total_jobs,
                   COALESCE(SUM(CASE WHEN status.name = 'EN_PROCESO' THEN 1 ELSE 0 END), 0)
                     AS in_progress_jobs,
                   COALESCE(SUM(CASE WHEN status.name = 'COMPLETADO' THEN 1 ELSE 0 END), 0)
                     AS completed_jobs
              FROM technicians technician
              JOIN users user ON user.id_users = technician.user_id
              LEFT JOIN jobs job
                ON job.technician_id = technician.id_technicians
               AND job.created_at >= ?
               AND job.created_at < ?
              LEFT JOIN status_catalog status
                ON status.id_status_catalog = job.status_id
               AND status.context = 'JOBS'
             GROUP BY technician.id_technicians, user.first_name, user.last_name
             ORDER BY total_jobs DESC, technician.id_technicians
            """;

    private final JdbcTemplate jdbc;

    public DashboardJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public DashboardSummary loadSummary(DashboardPeriod period) {
        List<DashboardSummary> results = queryForPeriod(SUMMARY_SQL, period,
                (rs, rowNum) -> new DashboardSummary(
                        rs.getLong("registered_customers"),
                        rs.getLong("registered_vehicles"),
                        rs.getLong("open_vehicle_intakes"),
                        rs.getLong("active_work_orders"),
                        rs.getLong("jobs_in_progress"),
                        money(rs, "period_revenue"),
                        "MXN", period.from(), period.to()));
        DashboardSummary result = results.isEmpty() ? null : results.get(0);
        return result == null ? emptySummary(period) : result;
    }

    @Override
    public List<StatusMetric> loadWorkOrdersByStatus(DashboardPeriod period) {
        return queryForPeriod(WORK_ORDERS_BY_STATUS_SQL, period, statusMapper());
    }

    @Override
    public List<StatusMetric> loadJobsByStatus(DashboardPeriod period) {
        return queryForPeriod(JOBS_BY_STATUS_SQL, period, statusMapper());
    }

    @Override
    public List<MonthlyRevenueMetric> loadRevenueByMonth(DashboardPeriod period) {
        return queryForPeriod(REVENUE_BY_MONTH_SQL, period, (rs, rowNum) ->
                new MonthlyRevenueMetric(rs.getString("period_code"),
                        rs.getInt("period_year"), rs.getInt("period_month"),
                        money(rs, "period_total")));
    }

    @Override
    public List<TopServiceMetric> loadTopServices(DashboardPeriod period, int limit) {
        return jdbc.query(TOP_SERVICES_SQL, parameters(period, limit), (rs, rowNum) ->
                new TopServiceMetric(rs.getLong("service_id"),
                        rs.getString("service_name"), money(rs, "performed_quantity")));
    }

    @Override
    public List<TechnicianWorkloadMetric> loadTechnicianWorkload(DashboardPeriod period) {
        return queryForPeriod(TECHNICIAN_WORKLOAD_SQL, period, (rs, rowNum) ->
                new TechnicianWorkloadMetric(rs.getLong("technician_id"),
                        rs.getString("technician_name"), rs.getLong("total_jobs"),
                        rs.getLong("in_progress_jobs"), rs.getLong("completed_jobs")));
    }

    private <T> List<T> queryForPeriod(String sql, DashboardPeriod period,
            RowMapper<T> mapper) {
        return jdbc.query(sql, parameters(period), mapper);
    }

    private PreparedStatementSetter parameters(DashboardPeriod period) {
        return statement -> bindPeriod(statement, period);
    }

    private PreparedStatementSetter parameters(DashboardPeriod period, int limit) {
        return statement -> {
            bindPeriod(statement, period);
            statement.setInt(3, limit);
        };
    }

    private void bindPeriod(PreparedStatement statement, DashboardPeriod period)
            throws SQLException {
        statement.setObject(1, period.fromInclusive());
        statement.setObject(2, period.toExclusive());
    }

    private RowMapper<StatusMetric> statusMapper() {
        return (rs, rowNum) -> {
            String code = rs.getString("status_code");
            return new StatusMetric(code, readableStatus(code), rs.getLong("metric_count"));
        };
    }

    private String readableStatus(String code) {
        String normalized = code.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private BigDecimal money(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    private DashboardSummary emptySummary(DashboardPeriod period) {
        return new DashboardSummary(0, 0, 0, 0, 0, BigDecimal.ZERO,
                "MXN", period.from(), period.to());
    }
}
