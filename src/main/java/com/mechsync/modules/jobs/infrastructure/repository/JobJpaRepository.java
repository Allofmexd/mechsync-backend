package com.mechsync.modules.jobs.infrastructure.repository;

import com.mechsync.modules.jobs.infrastructure.persistence.JobJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobJpaRepository extends JpaRepository<JobJpaEntity, Long> {
    Page<JobJpaEntity> findAllByTechnicianId(Long technicianId, Pageable pageable);
    Optional<JobJpaEntity> findByIdAndTechnicianId(Long id, Long technicianId);
    boolean existsByWorkOrderId(Long workOrderId);
    boolean existsByInitialApprovedRevisionId(Long initialApprovedRevisionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM JobJpaEntity j WHERE j.id = :id")
    Optional<JobJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM technicians WHERE id_technicians = :id", nativeQuery = true)
    long countTechniciansById(@Param("id") Long id);
}
