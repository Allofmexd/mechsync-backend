package com.mechsync.modules.workorders.application.dto;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import java.util.List;
public record WorkOrderPage(List<WorkOrder> content,int page,int size,long totalElements,int totalPages) { }
