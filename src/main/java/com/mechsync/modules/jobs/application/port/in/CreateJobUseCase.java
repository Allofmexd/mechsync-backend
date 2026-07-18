package com.mechsync.modules.jobs.application.port.in;

import com.mechsync.modules.jobs.application.dto.CreateJobCommand;
import com.mechsync.modules.jobs.domain.model.Job;

public interface CreateJobUseCase {
    Job create(CreateJobCommand command);
}
