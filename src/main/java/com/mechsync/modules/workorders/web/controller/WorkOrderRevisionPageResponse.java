package com.mechsync.modules.workorders.web.controller;

import com.mechsync.modules.workorders.application.dto.WorkOrderRevisionPage;
import java.util.List;

public record WorkOrderRevisionPageResponse(
        List<WorkOrderRevisionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public static WorkOrderRevisionPageResponse from(WorkOrderRevisionPage page) {
        return new WorkOrderRevisionPageResponse(
                page.content().stream().map(WorkOrderRevisionResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
