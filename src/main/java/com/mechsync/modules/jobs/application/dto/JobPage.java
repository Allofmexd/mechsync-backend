package com.mechsync.modules.jobs.application.dto;

import com.mechsync.modules.jobs.domain.model.Job;
import java.util.List;

public record JobPage(List<Job> content, int page, int size, long totalElements, int totalPages) {
}
