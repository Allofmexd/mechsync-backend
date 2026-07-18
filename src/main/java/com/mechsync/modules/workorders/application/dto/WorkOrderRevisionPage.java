package com.mechsync.modules.workorders.application.dto;

import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;
import java.util.List;

public record WorkOrderRevisionPage(
        List<WorkOrderRevision> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
