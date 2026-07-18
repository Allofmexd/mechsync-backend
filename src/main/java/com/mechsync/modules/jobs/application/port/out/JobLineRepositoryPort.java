package com.mechsync.modules.jobs.application.port.out;

import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JobLineRepositoryPort {
    Optional<String> findServiceName(Long serviceId);
    Optional<String> findPartName(Long partId);
    List<JobServiceLine> findServicesByJobId(Long jobId);
    Optional<JobServiceLine> findServiceByIdAndJobIdForUpdate(Long lineId, Long jobId);
    boolean serviceAlreadyUsed(Long jobId, Long serviceId, Long excludedLineId);
    JobServiceLine saveService(JobServiceLine line);
    void deleteService(Long lineId, Long jobId);
    List<JobPartLine> findPartsByJobId(Long jobId);
    Optional<JobPartLine> findPartByIdAndJobIdForUpdate(Long lineId, Long jobId);
    boolean partAlreadyUsed(Long jobId, Long partId, Long excludedLineId);
    JobPartLine savePart(JobPartLine line);
    void deletePart(Long lineId, Long jobId);
    BigDecimal calculateActualSubtotal(Long jobId);
}
