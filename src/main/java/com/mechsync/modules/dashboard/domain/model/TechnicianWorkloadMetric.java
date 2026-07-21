package com.mechsync.modules.dashboard.domain.model;

public record TechnicianWorkloadMetric(
        Long technicianId,
        String technicianName,
        long totalJobs,
        long inProgressJobs,
        long completedJobs) {
}
