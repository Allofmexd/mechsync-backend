package com.mechsync.modules.workorders.web.controller;
import com.mechsync.modules.workorders.application.dto.WorkOrderPage;
import java.util.List;
public record WorkOrderPageResponse(List<WorkOrderResponse> content,int page,int size,long totalElements,int totalPages){
 public static WorkOrderPageResponse from(WorkOrderPage p){return new WorkOrderPageResponse(
  p.content().stream().map(WorkOrderResponse::from).toList(),p.page(),p.size(),p.totalElements(),p.totalPages());}}
