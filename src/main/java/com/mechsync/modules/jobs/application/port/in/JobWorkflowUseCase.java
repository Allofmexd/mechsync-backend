package com.mechsync.modules.jobs.application.port.in;

import com.mechsync.modules.jobs.application.dto.CancelJobCommand;
import com.mechsync.modules.jobs.application.dto.CompleteJobCommand;
import com.mechsync.modules.jobs.domain.model.Job;

public interface JobWorkflowUseCase {
    Job start(Long id);
    Job complete(CompleteJobCommand command);
    Job cancel(CancelJobCommand command);
}
