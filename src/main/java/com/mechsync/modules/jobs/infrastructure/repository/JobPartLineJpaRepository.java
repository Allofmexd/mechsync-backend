package com.mechsync.modules.jobs.infrastructure.repository;

import com.mechsync.modules.jobs.infrastructure.persistence.JobPartLineJpaEntity;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobPartLineJpaRepository extends JpaRepository<JobPartLineJpaEntity, Long> {
    @Query(value = """
            SELECT line.id_job_parts AS lineId, line.job_id AS jobId,
                   line.part_id AS catalogId, catalog.name AS catalogName,
                   line.quantity_used AS quantity, line.actual_unit_price AS unitPrice,
                   line.actual_subtotal AS lineSubtotal, line.created_at AS createdAt,
                   line.updated_at AS updatedAt
            FROM job_parts line
            JOIN parts catalog ON catalog.id_parts = line.part_id
            WHERE line.job_id = :jobId
            ORDER BY line.id_job_parts
            """, nativeQuery = true)
    List<JobPartLineView> findViewsByJobId(@Param("jobId") Long jobId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT line FROM JobPartLineJpaEntity line "
            + "WHERE line.id = :lineId AND line.jobId = :jobId")
    Optional<JobPartLineJpaEntity> findByIdAndJobIdForUpdate(
            @Param("lineId") Long lineId, @Param("jobId") Long jobId);

    boolean existsByJobIdAndPartId(Long jobId, Long partId);
    boolean existsByJobIdAndPartIdAndIdNot(Long jobId, Long partId, Long id);

    @Query(value = "SELECT name FROM parts WHERE id_parts = :partId", nativeQuery = true)
    Optional<String> findPartName(@Param("partId") Long partId);

    @Query("SELECT COALESCE(SUM(line.actualSubtotal), 0) FROM JobPartLineJpaEntity line "
            + "WHERE line.jobId = :jobId")
    BigDecimal sumSubtotalByJobId(@Param("jobId") Long jobId);
}
