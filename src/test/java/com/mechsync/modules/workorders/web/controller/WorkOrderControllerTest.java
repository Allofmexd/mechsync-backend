package com.mechsync.modules.workorders.web.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.workorders.application.dto.WorkOrderPage;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.domain.exception.WorkOrderNotFoundException;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import com.mechsync.modules.technicians.application.port.in.ResolveAuthenticatedTechnicianUseCase;
import com.mechsync.modules.technicians.domain.exception.TechnicianProfileRequiredException;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.*;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
@WebMvcTest(value=WorkOrderController.class,properties={"debug=false",
 "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
 "mechsync.security.jwt.expiration-minutes=120","mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class,RestAuthenticationEntryPoint.class,
 RestAccessDeniedHandler.class,GlobalExceptionHandler.class})
class WorkOrderControllerTest {
 @Autowired MockMvc mvc;@MockitoBean JwtService jwt;@MockitoBean ListWorkOrdersUseCase list;
 @MockitoBean GetWorkOrderByIdUseCase get;@MockitoBean CreateWorkOrderUseCase create;
 @MockitoBean UpdateWorkOrderUseCase update;@MockitoBean DeleteWorkOrderUseCase delete;
 @MockitoBean ResolveAuthenticatedTechnicianUseCase technicianResolver;
 @Test void noTokenIs401()throws Exception{mvc.perform(get(path())).andExpect(status().isUnauthorized());
  mvc.perform(get(path()+"/assigned-to-me")).andExpect(status().isUnauthorized());}
 @Test void technicianUsesOnlyAssignedEndpointsAndCannotWrite()throws Exception{token("tech","TECNICO");assignedStubs();
  mvc.perform(auth(get(path()),"tech")).andExpect(status().isForbidden());
  mvc.perform(auth(get(path()+"/assigned-to-me"),"tech")).andExpect(status().isOk())
   .andExpect(jsonPath("$.data.totalElements").value(1));
  mvc.perform(auth(get(path()+"/1"),"tech")).andExpect(status().isOk());
  mvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"tech")).andExpect(status().isForbidden());
  mvc.perform(auth(put(path()+"/1").contentType(MediaType.APPLICATION_JSON).content(updateBody()),"tech")).andExpect(status().isForbidden());}
 @Test void technicianGets404ForForeignOrder()throws Exception{token("tech","TECNICO");
  when(technicianResolver.resolveId(any())).thenReturn(2L);
  when(get.getAssignedTo(99L,2L)).thenThrow(new WorkOrderNotFoundException(99L));
  mvc.perform(auth(get(path()+"/99"),"tech")).andExpect(status().isNotFound());}
 @Test void technicianWithoutProfileGetsControlled403()throws Exception{token("tech","TECNICO");
  when(technicianResolver.resolveId(any())).thenThrow(new TechnicianProfileRequiredException());
  mvc.perform(auth(get(path()+"/assigned-to-me"),"tech")).andExpect(status().isForbidden());}
 @Test void technicianCannotDelete()throws Exception{token("tech","TECNICO");mvc.perform(auth(delete(path()+"/1"),"tech")).andExpect(status().isForbidden());}
 @Test void clientCannotReadOrCreate()throws Exception{token("client","CLIENTE");mvc.perform(auth(get(path()),"client")).andExpect(status().isForbidden());
  mvc.perform(auth(get(path()+"/assigned-to-me"),"client")).andExpect(status().isForbidden());
  mvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"client")).andExpect(status().isForbidden());}
 @Test void adminCanOperateAll()throws Exception{token("admin","ADMINISTRADOR");stubs();
  mvc.perform(auth(get(path()),"admin")).andExpect(status().isOk());mvc.perform(auth(get(path()+"/1"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"admin")).andExpect(status().isCreated());
  mvc.perform(auth(put(path()+"/1").contentType(MediaType.APPLICATION_JSON).content(updateBody()),"admin")).andExpect(status().isOk());
  mvc.perform(auth(delete(path()+"/1"),"admin")).andExpect(status().isNoContent());
  org.mockito.Mockito.verifyNoInteractions(technicianResolver);}
 @Test void invalidRequestIs400()throws Exception{token("admin","ADMINISTRADOR");mvc.perform(auth(post(path())
  .contentType(MediaType.APPLICATION_JSON).content("{\"vehicleIntakeId\":0,\"technicianId\":0,\"estimatedSubtotal\":-1,\"estimatedIva\":-1,\"estimatedTotal\":-1,\"statusId\":0}"),"admin"))
  .andExpect(status().isBadRequest());}
 @Test void missingOrderIs404()throws Exception{token("admin","ADMINISTRADOR");when(get.getById(99L)).thenThrow(new WorkOrderNotFoundException(99L));
  mvc.perform(auth(get(path()+"/99"),"admin")).andExpect(status().isNotFound());}
 private void stubs(){when(list.list(0,20)).thenReturn(new WorkOrderPage(List.of(order()),0,20,1,1));when(get.getById(1L)).thenReturn(order());
  when(create.create(any())).thenReturn(order());when(update.update(any())).thenReturn(order());}
 private void assignedStubs(){when(technicianResolver.resolveId(any())).thenReturn(2L);
  when(list.listAssignedTo(2L,0,20)).thenReturn(new WorkOrderPage(List.of(order()),0,20,1,1));
  when(get.getAssignedTo(1L,2L)).thenReturn(order());}
 private void token(String t,String role){when(jwt.parse(t)).thenReturn(new AuthenticatedUser(1L,"user@mechsync.local",Set.of(role)));}
 private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder r,String t){return r.header("Authorization","Bearer "+t);}
 private String path(){return "/api/v1/work-orders";}
 private String createBody(){return "{\"vehicleIntakeId\":1,\"technicianId\":2,\"estimatedHours\":2.00,\"estimatedSubtotal\":100.00,\"estimatedIva\":16.00,\"estimatedTotal\":116.00,\"technicalObservations\":\"Planning\",\"statusId\":12}";}
 private String updateBody(){return "{\"technicianId\":2,\"estimatedHours\":2.00,\"estimatedSubtotal\":100.00,\"estimatedIva\":16.00,\"estimatedTotal\":116.00,\"technicalObservations\":\"Planning\",\"statusId\":12}";}
 private WorkOrder order(){return new WorkOrder(1L,1L,2L,LocalDateTime.of(2026,7,12,10,0),null,null,new BigDecimal("2.00"),
  new BigDecimal("100.00"),new BigDecimal("16.00"),new BigDecimal("116.00"),"Planning",12L,LocalDateTime.now(),null);}
}
