package com.mechsync.modules.customerportal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CustomerPortalOperationsSqlContractTest {

    @Test
    void intakeQueriesEnforceOwnershipAndStablePagination() {
        assertContains(CustomerPortalIntakeJdbcAdapter.LIST_SQL,
                "v.customer_id = ?", "ORDER BY vi.intake_date DESC",
                "vi.id_vehicle_intakes DESC", "LIMIT ? OFFSET ?");
        assertContains(CustomerPortalIntakeJdbcAdapter.COUNT_SQL,
                "v.customer_id = ?", "v.id_vehicles = ?");
        assertContains(CustomerPortalIntakeJdbcAdapter.DETAIL_SQL,
                "vi.id_vehicle_intakes = ?", "v.customer_id = ?");
        assertFalse(CustomerPortalIntakeJdbcAdapter.LIST_SQL.contains("initial_observations"));
        assertFalse(CustomerPortalIntakeJdbcAdapter.LIST_SQL.contains("technician_id"));
    }

    @Test
    void workOrderQueriesKeepOwnershipAndRevisionVisibilityInsideSql() {
        assertContains(CustomerPortalWorkOrderJdbcAdapter.LIST_SQL,
                "v.customer_id = ?", "APPROVED", "SENT", "LIMIT ? OFFSET ?");
        assertContains(CustomerPortalWorkOrderJdbcAdapter.COUNT_SQL,
                "v.customer_id = ?", "current_revision_id", "final_approved_revision_id");
        assertContains(CustomerPortalWorkOrderJdbcAdapter.DETAIL_SQL,
                "wo.id_work_orders = ?", "v.customer_id = ?");
        assertContains(CustomerPortalWorkOrderJdbcAdapter.QUOTATION_SQL,
                "wo.id_work_orders = ?", "v.customer_id = ?",
                "rs.code = 'APPROVED'", "rs.code = 'SENT'", "LIMIT 1");
        assertFalse(CustomerPortalWorkOrderJdbcAdapter.QUOTATION_SQL.contains("created_by_user_id"));
        assertFalse(CustomerPortalWorkOrderJdbcAdapter.QUOTATION_SQL.contains("approved_by_user_id"));
        assertFalse(CustomerPortalWorkOrderJdbcAdapter.DETAIL_SQL.contains("technical_observations"));
        assertFalse(CustomerPortalWorkOrderJdbcAdapter.DETAIL_SQL.contains("estimated_total"));
    }

    @Test
    void jobQueriesDoNotExposeContactsOrProvisionalFinancialTotals() {
        assertContains(CustomerPortalJobJdbcAdapter.LIST_SQL,
                "v.customer_id = ?", "LIMIT ? OFFSET ?");
        assertContains(CustomerPortalJobJdbcAdapter.COUNT_SQL, "v.customer_id = ?");
        assertContains(CustomerPortalJobJdbcAdapter.DETAIL_SQL,
                "j.id_jobs = ?", "v.customer_id = ?");
        assertContains(CustomerPortalJobJdbcAdapter.SERVICES_SQL,
                "job_services", "job_id = ?");
        assertContains(CustomerPortalJobJdbcAdapter.PARTS_SQL,
                "job_parts", "job_id = ?");
        String customerJobSql = CustomerPortalJobJdbcAdapter.LIST_SQL
                + CustomerPortalJobJdbcAdapter.DETAIL_SQL;
        assertFalse(customerJobSql.contains("email"));
        assertFalse(customerJobSql.contains("phone"));
        assertFalse(customerJobSql.contains("actual_total"));
        assertFalse(customerJobSql.contains("execution_notes"));
    }

    @Test
    void historyUsesOneOwnedUnionWithGlobalCountAndStableOrder() {
        assertContains(CustomerPortalHistoryJdbcAdapter.LIST_SQL,
                "WITH owned_vehicles", "UNION ALL", "v.customer_id = ?",
                "ORDER BY event_date DESC, event_type, event_id DESC",
                "LIMIT ? OFFSET ?");
        assertContains(CustomerPortalHistoryJdbcAdapter.COUNT_SQL,
                "WITH owned_vehicles", "SELECT COUNT(*)", "UNION ALL");
        for (String type : new String[] {
                "VEHICLE_INTAKE", "WORK_ORDER", "QUOTATION_AVAILABLE",
                "QUOTATION_APPROVED", "JOB_STARTED", "JOB_COMPLETED"
        }) {
            assertTrue(CustomerPortalHistoryJdbcAdapter.LIST_SQL.contains(type), type);
        }
        assertFalse(CustomerPortalHistoryJdbcAdapter.LIST_SQL.contains("SERVICE_REPORT"));
    }

    private void assertContains(String sql, String... fragments) {
        for (String fragment : fragments) {
            assertTrue(sql.contains(fragment), () -> "Missing SQL fragment: " + fragment);
        }
    }
}
