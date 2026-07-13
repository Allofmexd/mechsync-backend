package com.mechsync.modules.vehicleintakes.web.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
import com.mechsync.modules.vehicleintakes.application.port.in.*;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeNotFoundException;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.*;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value=VehicleIntakeController.class, properties={"debug=false",
 "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
 "mechsync.security.jwt.expiration-minutes=120","mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class,RestAuthenticationEntryPoint.class,
 RestAccessDeniedHandler.class,GlobalExceptionHandler.class})
class VehicleIntakeControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean ListVehicleIntakesUseCase listUseCase;
    @MockitoBean GetVehicleIntakeByIdUseCase getUseCase;
    @MockitoBean CreateVehicleIntakeUseCase createUseCase;
    @MockitoBean UpdateVehicleIntakeUseCase updateUseCase;
    @MockitoBean DeleteVehicleIntakeUseCase deleteUseCase;

    @Test void noTokenIsUnauthorized() throws Exception { mockMvc.perform(get(path())).andExpect(status().isUnauthorized()); }
    @Test void technicianCanReadCreateAndUpdate() throws Exception {
        token("tech","TECNICO"); when(listUseCase.list(0,20)).thenReturn(new VehicleIntakePage(List.of(intake()),0,20,1,1));
        when(getUseCase.getById(1L)).thenReturn(intake()); when(createUseCase.create(any())).thenReturn(intake());
        when(updateUseCase.update(any())).thenReturn(intake());
        mockMvc.perform(auth(get(path()),"tech")).andExpect(status().isOk());
        mockMvc.perform(auth(get(path()+"/1"),"tech")).andExpect(status().isOk());
        mockMvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"tech"))
                .andExpect(status().isCreated());
        mockMvc.perform(auth(put(path()+"/1").contentType(MediaType.APPLICATION_JSON).content(updateBody()),"tech"))
                .andExpect(status().isOk());
    }
    @Test void technicianCannotDelete() throws Exception { token("tech","TECNICO");
        mockMvc.perform(auth(delete(path()+"/1"),"tech")).andExpect(status().isForbidden()); }
    @Test void customerCannotListOrCreate() throws Exception { token("customer","CLIENTE");
        mockMvc.perform(auth(get(path()),"customer")).andExpect(status().isForbidden());
        mockMvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"customer"))
                .andExpect(status().isForbidden()); }
    @Test void administratorCanOperateAllEndpoints() throws Exception {
        token("admin","ADMINISTRADOR");
        when(listUseCase.list(0,20)).thenReturn(new VehicleIntakePage(List.of(intake()),0,20,1,1));
        when(getUseCase.getById(1L)).thenReturn(intake());
        when(createUseCase.create(any())).thenReturn(intake());
        when(updateUseCase.update(any())).thenReturn(intake());
        mockMvc.perform(auth(get(path()),"admin")).andExpect(status().isOk());
        mockMvc.perform(auth(get(path()+"/1"),"admin")).andExpect(status().isOk());
        mockMvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON).content(createBody()),"admin"))
                .andExpect(status().isCreated());
        mockMvc.perform(auth(put(path()+"/1").contentType(MediaType.APPLICATION_JSON).content(updateBody()),"admin"))
                .andExpect(status().isOk());
        mockMvc.perform(auth(delete(path()+"/1"),"admin")).andExpect(status().isNoContent());
    }
    @Test void invalidRequestIsBadRequest() throws Exception { token("admin","ADMINISTRADOR");
        mockMvc.perform(auth(post(path()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleId\":0,\"intakeMileage\":-1,\"reportedProblem\":\"\",\"statusId\":0}"),"admin"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.message",is("Validation failed"))); }
    @Test void missingIntakeIsNotFound() throws Exception { token("admin","ADMINISTRADOR");
        when(getUseCase.getById(99L)).thenThrow(new VehicleIntakeNotFoundException(99L));
        mockMvc.perform(auth(get(path()+"/99"),"admin")).andExpect(status().isNotFound()); }
    private void token(String value,String role) { when(jwtService.parse(value)).thenReturn(
            new AuthenticatedUser(1L,"user@mechsync.local",Set.of(role))); }
    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder r,String token) {
        return r.header("Authorization","Bearer "+token); }
    private String path() { return "/api/v1/vehicle-intakes"; }
    private String createBody() { return "{\"vehicleId\":2,\"technicianId\":3,\"intakeMileage\":100,"
            +"\"reportedProblem\":\"Slipping\",\"initialObservations\":\"Noise\",\"statusId\":7}"; }
    private String updateBody() { return "{\"technicianId\":3,\"intakeMileage\":120,"
            +"\"reportedProblem\":\"Slipping\",\"initialObservations\":\"Noise\",\"statusId\":8}"; }
    private VehicleIntake intake() { return new VehicleIntake(1L,2L,3L,LocalDateTime.of(2026,7,12,10,30),
            100,"Slipping","Noise",7L,LocalDateTime.of(2026,7,12,10,30),null); }
}
