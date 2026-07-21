package com.mechsync.modules.jobs.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobLineOwnershipMySqlIT {
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

    @Autowired JobJpaRepository jobs;
    @Autowired JobServiceLineJpaRepository services;
    @Autowired JobPartLineJpaRepository parts;
    @Autowired JdbcTemplate jdbc;

    @Test
    void resolvesOwnershipBeforeReadingRealLineQueries() {
        assertEquals(QA_DATABASE, jdbc.queryForObject("SELECT DATABASE()", String.class));

        List<AssignedJob> assignedJobs = jdbc.query("""
                SELECT id_jobs, technician_id
                FROM jobs
                WHERE technician_id IS NOT NULL
                ORDER BY id_jobs DESC
                """, (rs, rowNum) -> new AssignedJob(
                rs.getLong("id_jobs"), rs.getLong("technician_id")));

        AssignedJob jobA = assignedJobs.stream().findFirst()
                .orElseThrow(() -> new AssertionError("QA must contain an assigned Job"));
        AssignedJob jobB = assignedJobs.stream()
                .filter(job -> !job.technicianId().equals(jobA.technicianId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "QA must contain Jobs for at least two technicians"));

        assertTrue(jobs.findByIdAndTechnicianId(jobA.jobId(), jobA.technicianId()).isPresent());
        assertTrue(jobs.findByIdAndTechnicianId(jobB.jobId(), jobB.technicianId()).isPresent());
        assertFalse(jobs.findByIdAndTechnicianId(jobA.jobId(), jobB.technicianId()).isPresent());
        assertFalse(jobs.findByIdAndTechnicianId(jobB.jobId(), jobA.technicianId()).isPresent());

        assertTrue(services.findViewsByJobId(jobA.jobId()).stream()
                .allMatch(line -> jobA.jobId().equals(line.getJobId())));
        assertTrue(parts.findViewsByJobId(jobA.jobId()).stream()
                .allMatch(line -> jobA.jobId().equals(line.getJobId())));
        assertTrue(services.findViewsByJobId(jobB.jobId()).stream()
                .allMatch(line -> jobB.jobId().equals(line.getJobId())));
        assertTrue(parts.findViewsByJobId(jobB.jobId()).stream()
                .allMatch(line -> jobB.jobId().equals(line.getJobId())));
    }

    private record AssignedJob(Long jobId, Long technicianId) {
    }
}
