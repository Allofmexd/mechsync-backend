package com.mechsync.modules.jobs.application.port.in;

import com.mechsync.modules.jobs.application.dto.JobPage;
import com.mechsync.modules.jobs.domain.model.Job;

public interface JobQueryUseCase {
    JobPage list(int page, int size);
    Job get(Long id);
}
