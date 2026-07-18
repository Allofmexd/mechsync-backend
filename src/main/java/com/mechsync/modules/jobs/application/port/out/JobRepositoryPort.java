package com.mechsync.modules.jobs.application.port.out;

import com.mechsync.modules.jobs.application.dto.JobPage;
import com.mechsync.modules.jobs.domain.model.Job;
import com.mechsync.modules.jobs.domain.model.JobStatus;
import java.math.BigDecimal;
import java.util.Optional;

public interface JobRepositoryPort {
    boolean workOrderExists(Long workOrderId);
    Optional<Long> finalApprovedRevisionId(Long workOrderId);
    Optional<JobRevisionAuthorization> findRevisionAuthorization(Long revisionId);
    boolean technicianExists(Long technicianId);
    boolean existsByWorkOrderId(Long workOrderId);
    boolean existsByInitialApprovedRevisionId(Long revisionId);
    Long requireStatusId(JobStatus status);
    Job insert(Job job, Long statusId);
    JobPage findAll(int page, int size);
    Optional<Job> findById(Long id);
    Optional<Job> findByIdForUpdate(Long id);
    Job update(Job job, Long statusId);
    void updateActualSubtotal(Long jobId, BigDecimal actualSubtotal);
}
