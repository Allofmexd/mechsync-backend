package com.mechsync.modules.servicereports.infrastructure.repository;

import com.mechsync.modules.servicereports.infrastructure.persistence.ServiceReportJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceReportJpaRepository extends JpaRepository<ServiceReportJpaEntity, Long> {
    boolean existsByJobId(Long jobId);
    Optional<ServiceReportJpaEntity> findByJobId(Long jobId);

    @Query(value = """
            SELECT j.id_jobs AS jobId, sc.name AS statusCode,
                   j.actual_subtotal AS actualSubtotal, j.actual_iva AS actualIva,
                   j.actual_total AS actualTotal
            FROM jobs j
            JOIN status_catalog sc ON sc.id_status_catalog = j.status_id
            WHERE j.id_jobs = :jobId AND sc.context = 'JOBS'
            """, nativeQuery = true)
    Optional<JobClosureView> findJobClosure(@Param("jobId") Long jobId);
}
