package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.application.dto.JobPage;
import java.util.List;

public record JobPageResponse(
        List<JobResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public static JobPageResponse from(JobPage result) {
        return new JobPageResponse(result.content().stream().map(JobResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
