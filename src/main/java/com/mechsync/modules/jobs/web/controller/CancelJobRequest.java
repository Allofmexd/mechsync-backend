package com.mechsync.modules.jobs.web.controller;

import jakarta.validation.constraints.Size;

public record CancelJobRequest(@Size(max = 500) String cancellationNotes) {
}
