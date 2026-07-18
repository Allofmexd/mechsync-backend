package com.mechsync.modules.workorders.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.workorders.application.dto.WorkOrderRevisionPage;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.domain.model.*;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.*;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value=WorkOrderRevisionController.class,properties={"debug=false",
 "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
 "mechsync.security.jwt.expiration-minutes=120","mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class,RestAuthenticationEntryPoint.class,
 RestAccessDeniedHandler.class,GlobalExceptionHandler.class})
class WorkOrderRevisionControllerTest {
 @Autowired MockMvc mvc;
 @MockitoBean JwtService jwt;
 @MockitoBean WorkOrderRevisionQueryUseCase query;
 @MockitoBean CreateWorkOrderRevisionUseCase create;
 @MockitoBean WorkOrderRevisionWorkflowUseCase workflow;

 @Test void noTokenIs401()throws Exception{mvc.perform(get(base())).andExpect(status().isUnauthorized());}

 @Test void clientIsForbidden()throws Exception{token("client","CLIENTE");
  mvc.perform(auth(get(base()),"client")).andExpect(status().isForbidden());}

 @Test void technicianCanReadButCannotMutate()throws Exception{token("tech","TECNICO");stubs();
  mvc.perform(auth(get(base()),"tech")).andExpect(status().isOk());
  mvc.perform(auth(get(base()+"/1"),"tech")).andExpect(status().isOk());
  mvc.perform(auth(post(base()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"tech"))
   .andExpect(status().isForbidden());
  mvc.perform(auth(patch(base()+"/1/send"),"tech")).andExpect(status().isForbidden());}

 @Test void administratorCanUseAllRevisionEndpoints()throws Exception{token("admin","ADMINISTRADOR");stubs();
  mvc.perform(auth(get(base()),"admin")).andExpect(status().isOk());
  mvc.perform(auth(get(base()+"/current"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(get(base()+"/final-approved"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(get(base()+"/1"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(post(base()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"admin"))
   .andExpect(status().isCreated()).andExpect(header().string("Location",base()+"/1"));
  mvc.perform(auth(patch(base()+"/1/send"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(patch(base()+"/1/approve").contentType(MediaType.APPLICATION_JSON)
   .content("{\"acceptedByName\":\"Customer\",\"acceptanceMethod\":\"IN_PERSON\"}"),"admin"))
   .andExpect(status().isOk());
  mvc.perform(auth(patch(base()+"/1/reject"),"admin")).andExpect(status().isOk());
  mvc.perform(auth(patch(base()+"/1/cancel"),"admin")).andExpect(status().isOk());}

 @Test void approvalRequiresAcceptedName()throws Exception{token("admin","ADMINISTRADOR");
  mvc.perform(auth(patch(base()+"/1/approve").contentType(MediaType.APPLICATION_JSON)
   .content("{\"acceptedByName\":\"   \",\"acceptanceMethod\":\"IN_PERSON\"}"),"admin"))
   .andExpect(status().isBadRequest());}

 @Test void apiV2RouteDoesNotExist()throws Exception{token("admin","ADMINISTRADOR");
  mvc.perform(auth(get("/api/v2/work-orders/1/revisions"),"admin")).andExpect(status().isNotFound());}

 private void stubs(){WorkOrderRevision r=revision();
  when(query.list(eq(1L),eq(0),eq(20),any())).thenReturn(new WorkOrderRevisionPage(List.of(r),0,20,1,1));
  when(query.get(eq(1L),eq(1L),any())).thenReturn(r);when(query.getCurrent(eq(1L),any())).thenReturn(r);
  when(query.getFinalApproved(eq(1L),any())).thenReturn(r);when(create.create(any())).thenReturn(r);
  when(workflow.send(eq(1L),eq(1L),any())).thenReturn(r);when(workflow.approve(any())).thenReturn(r);
  when(workflow.reject(eq(1L),eq(1L),any())).thenReturn(r);when(workflow.cancel(eq(1L),eq(1L),any())).thenReturn(r);}
 private void token(String value,String role){when(jwt.parse(value)).thenReturn(
  new AuthenticatedUser(99L,"user@mechsync.local",Set.of(role)));}
 private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request,String token){
  return request.header("Authorization","Bearer "+token);}
 private String base(){return "/api/v1/work-orders/1/revisions";}
 private String createBody(){return "{\"technicianId\":2,\"applyIva\":true,\"subtotalAmount\":100.00,"
  +"\"ivaAmount\":16.00,\"totalAmount\":116.00,\"services\":[],\"parts\":[]}";}
 private WorkOrderRevision revision(){return new WorkOrderRevision(1L,1L,1,WorkOrderRevisionStatus.DRAFT,2L,
  null,null,new BigDecimal("1.0000"),new BigDecimal("100.00"),true,new BigDecimal("0.160000"),
  new BigDecimal("16.00"),new BigDecimal("116.00"),"MXN",null,"Notes",null,null,99L,null,null,null,
  null,null,null,null,0,LocalDateTime.now(),null,true,false,List.of(),List.of());}
}
