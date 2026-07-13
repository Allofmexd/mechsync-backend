package com.mechsync.modules.workorders.web.controller;
import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@RestController @Validated @RequestMapping(ApiPaths.WORK_ORDERS)
public class WorkOrderController {
 private final ListWorkOrdersUseCase list;private final GetWorkOrderByIdUseCase get;private final CreateWorkOrderUseCase create;
 private final UpdateWorkOrderUseCase update;private final DeleteWorkOrderUseCase delete;
 public WorkOrderController(ListWorkOrdersUseCase list,GetWorkOrderByIdUseCase get,CreateWorkOrderUseCase create,
  UpdateWorkOrderUseCase update,DeleteWorkOrderUseCase delete){this.list=list;this.get=get;this.create=create;this.update=update;this.delete=delete;}
 @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
 public ApiResponse<WorkOrderPageResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,
  @RequestParam(defaultValue="20") @Min(1) @Max(100) int size){return ApiResponse.ok(WorkOrderPageResponse.from(list.list(page,size)));}
 @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
 public ApiResponse<WorkOrderResponse> get(@PathVariable @Positive Long id){return ApiResponse.ok(WorkOrderResponse.from(get.getById(id)));}
 @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
 public ResponseEntity<ApiResponse<WorkOrderResponse>> create(@Valid @RequestBody CreateWorkOrderRequest r){
  WorkOrder o=create.create(new CreateWorkOrderCommand(r.vehicleIntakeId(),r.technicianId(),r.workOrderDate(),
   r.estimatedStartDate(),r.estimatedDeliveryDate(),r.estimatedHours(),r.estimatedSubtotal(),r.estimatedIva(),
   r.estimatedTotal(),r.technicalObservations(),r.statusId()));
  return ResponseEntity.created(URI.create(ApiPaths.WORK_ORDERS+"/"+o.id())).body(ApiResponse.ok(WorkOrderResponse.from(o)));}
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
 public ApiResponse<WorkOrderResponse> update(@PathVariable @Positive Long id,@Valid @RequestBody UpdateWorkOrderRequest r){
  return ApiResponse.ok(WorkOrderResponse.from(update.update(new UpdateWorkOrderCommand(id,r.technicianId(),
   r.workOrderDate(),r.estimatedStartDate(),r.estimatedDeliveryDate(),r.estimatedHours(),r.estimatedSubtotal(),
   r.estimatedIva(),r.estimatedTotal(),r.technicalObservations(),r.statusId()))));}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
 public ResponseEntity<Void> delete(@PathVariable @Positive Long id){delete.delete(id);return ResponseEntity.noContent().build();}
}
