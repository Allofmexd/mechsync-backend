package com.mechsync.modules.servicereports.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mechsync.modules.servicereports.infrastructure.persistence.ServiceReportJpaEntity;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceReportJpaRepositoryMySqlIT {
    private static final String QA_DATABASE = "mechsync_security_qa";

    @DynamicPropertySource
    static void configureQaDataSource(DynamicPropertyRegistry registry) {
        String url = System.getenv("MECHSYNC_DB_URL");
        String username = System.getenv("MECHSYNC_DB_USERNAME");
        String password = System.getenv("MECHSYNC_DB_PASSWORD");
        if (url == null
                || !url.startsWith("jdbc:mysql://localhost:3307/")
                || !url.contains("/" + QA_DATABASE)) {
            throw new IllegalStateException(
                    "This integration test only runs against localhost:3307/" + QA_DATABASE);
        }
        if (!"mechsync_security_qa_app".equals(username)
                || password == null || password.isBlank()) {
            throw new IllegalStateException("Missing isolated QA database credentials");
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
    }

    @Autowired
    ServiceReportJpaRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void filtersOrdersAndPaginatesAssignedReportsUsingRealMySqlColumns() {
        assertEquals(QA_DATABASE, jdbc.queryForObject("SELECT DATABASE()", String.class));

        List<TechnicianReportCount> technicians = jdbc.query("""
                SELECT j.technician_id, COUNT(*) AS report_count
                FROM service_reports sr
                INNER JOIN jobs j ON j.id_jobs = sr.job_id
                GROUP BY j.technician_id
                ORDER BY report_count DESC, j.technician_id
                """, (rs, rowNum) -> new TechnicianReportCount(
                rs.getLong("technician_id"), rs.getLong("report_count")));

        assertTrue(technicians.size() >= 2,
                "QA must contain reports assigned to at least two technicians");
        TechnicianReportCount technicianA = technicians.stream()
                .filter(value -> value.reportCount() >= 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "QA must contain at least two reports for one technician"));
        TechnicianReportCount technicianB = technicians.stream()
                .filter(value -> !value.technicianId().equals(technicianA.technicianId()))
                .findFirst()
                .orElseThrow();

        List<Long> expectedA = reportIds(technicianA.technicianId());
        List<Long> expectedB = reportIds(technicianB.technicianId());

        Page<ServiceReportJpaEntity> firstPageA = repository.findAllByTechnicianId(
                technicianA.technicianId(), PageRequest.of(0, 1));
        Page<ServiceReportJpaEntity> secondPageA = repository.findAllByTechnicianId(
                technicianA.technicianId(), PageRequest.of(1, 1));
        Page<ServiceReportJpaEntity> pageB = repository.findAllByTechnicianId(
                technicianB.technicianId(), PageRequest.of(0, 100));

        assertEquals(expectedA.size(), firstPageA.getTotalElements());
        assertEquals(expectedA.get(0), firstPageA.getContent().get(0).getId());
        assertEquals(expectedA.get(1), secondPageA.getContent().get(0).getId());
        assertEquals(expectedA.size(), secondPageA.getTotalElements());
        assertEquals(expectedB.size(), pageB.getTotalElements());
        assertEquals(expectedB, pageB.getContent().stream()
                .map(ServiceReportJpaEntity::getId).toList());

        HashSet<Long> idsA = new HashSet<>(expectedA);
        assertTrue(expectedB.stream().noneMatch(idsA::contains),
                "Assigned report sets must remain disjoint");
    }

    private List<Long> reportIds(Long technicianId) {
        return jdbc.queryForList("""
                SELECT sr.id_service_reports
                FROM service_reports sr
                INNER JOIN jobs j ON j.id_jobs = sr.job_id
                WHERE j.technician_id = ?
                ORDER BY sr.id_service_reports DESC
                """, Long.class, technicianId);
    }

    private record TechnicianReportCount(Long technicianId, long reportCount) {
    }
}
