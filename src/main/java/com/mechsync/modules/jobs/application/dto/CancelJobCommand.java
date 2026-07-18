package com.mechsync.modules.jobs.application.dto;

public record CancelJobCommand(Long jobId, String cancellationNotes) {
}
