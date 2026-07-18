package com.mechsync.modules.jobs.infrastructure.repository;

import com.mechsync.modules.jobs.infrastructure.persistence.JobServiceLineJpaEntity;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobServiceLineJpaRepository
        extends JpaRepository<JobServiceLineJpaEntity, Long> {
    @Query(value = """
            SELECT line.id_job_services AS lineId, line.job_id AS jobId,
                   line.service_id AS catalogId, catalog.name AS catalogName,
                   line.quantity AS quantity, line.actual_unit_price AS unitPrice,
                   line.actual_subtotal AS lineSubtotal, line.created_at AS createdAt,
                   line.updated_at AS updatedAt
            FROM job_services line
            JOIN services catalog ON catalog.id_services = line.service_id
            WHERE line.job_id = :jobId
            ORDER BY line.id_job_services
            """, nativeQuery = true)
    List<JobServiceLineView> findViewsByJobId(@Param("jobId") Long jobId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT line FROM JobServiceLineJpaEntity line "
            + "WHERE line.id = :lineId AND line.jobId = :jobId")
    Optional<JobServiceLineJpaEntity> findByIdAndJobIdForUpdate(
            @Param("lineId") Long lineId, @Param("jobId") Long jobId);

    boolean existsByJobIdAndServiceId(Long jobId, Long serviceId);
    boolean existsByJobIdAndServiceIdAndIdNot(Long jobId, Long serviceId, Long id);

    @Query(value = "SELECT name FROM services WHERE id_services = :serviceId", nativeQuery = true)
    Optional<String> findServiceName(@Param("serviceId") Long serviceId);

    @Query("SELECT COALESCE(SUM(line.actualSubtotal), 0) FROM JobServiceLineJpaEntity line "
            + "WHERE line.jobId = :jobId")
    BigDecimal sumSubtotalByJobId(@Param("jobId") Long jobId);
}
