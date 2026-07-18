package com.mechsync.modules.jobs.infrastructure.repository;

import com.mechsync.modules.jobs.infrastructure.persistence.JobJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobJpaRepository extends JpaRepository<JobJpaEntity, Long> {
    boolean existsByWorkOrderId(Long workOrderId);
    boolean existsByInitialApprovedRevisionId(Long initialApprovedRevisionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM JobJpaEntity j WHERE j.id = :id")
    Optional<JobJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM technicians WHERE id_technicians = :id", nativeQuery = true)
    long countTechniciansById(@Param("id") Long id);
}
