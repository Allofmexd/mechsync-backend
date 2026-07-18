package com.mechsync.modules.jobs.application.port.in;

import com.mechsync.modules.jobs.application.dto.UpsertJobPartLineCommand;
import com.mechsync.modules.jobs.application.dto.UpsertJobServiceLineCommand;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import java.util.List;

public interface JobLineUseCase {
    List<JobServiceLine> listServices(Long jobId);
    JobServiceLine addService(UpsertJobServiceLineCommand command);
    JobServiceLine updateService(UpsertJobServiceLineCommand command);
    void deleteService(Long jobId, Long lineId);
    List<JobPartLine> listParts(Long jobId);
    JobPartLine addPart(UpsertJobPartLineCommand command);
    JobPartLine updatePart(UpsertJobPartLineCommand command);
    void deletePart(Long jobId, Long lineId);
}
