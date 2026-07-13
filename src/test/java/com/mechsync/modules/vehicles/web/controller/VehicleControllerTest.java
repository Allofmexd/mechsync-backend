package com.mechsync.modules.vehicles.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.application.port.in.CreateVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.DeleteVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.GetVehicleByIdUseCase;
import com.mechsync.modules.vehicles.application.port.in.ListVehiclesUseCase;
import com.mechsync.modules.vehicles.application.port.in.UpdateVehicleUseCase;
import com.mechsync.modules.vehicles.domain.exception.VehicleNotFoundException;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value = VehicleController.class, properties = {
        "debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class VehicleControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private ListVehiclesUseCase listVehiclesUseCase;
    @MockitoBean private GetVehicleByIdUseCase getVehicleByIdUseCase;
    @MockitoBean private CreateVehicleUseCase createVehicleUseCase;
    @MockitoBean private UpdateVehicleUseCase updateVehicleUseCase;
    @MockitoBean private DeleteVehicleUseCase deleteVehicleUseCase;

    @Test
    void listWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void technicianCanListAndGetVehicles() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listVehiclesUseCase.list(0, 20)).thenReturn(
                new VehiclePage(List.of(vehicle()), 0, 20, 1, 1));
        when(getVehicleByIdUseCase.getById(1L)).thenReturn(vehicle());

        mockMvc.perform(authenticated(get("/api/v1/vehicles"), "technician-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
        mockMvc.perform(authenticated(get("/api/v1/vehicles/1"), "technician-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.licensePlate", is("ABC123")));
    }

    @Test
    void technicianCannotCreateUpdateOrDelete() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticated(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateBody()), "technician-token"))
                .andExpect(status().isForbidden());
        mockMvc.perform(authenticated(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON).content(validUpdateBody()), "technician-token"))
                .andExpect(status().isForbidden());
        mockMvc.perform(authenticated(delete("/api/v1/vehicles/1"), "technician-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotListOrCreateVehicles() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get("/api/v1/vehicles"), "customer-token"))
                .andExpect(status().isForbidden());
        mockMvc.perform(authenticated(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateBody()), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateVehicle() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(createVehicleUseCase.create(any())).thenReturn(vehicle());

        mockMvc.perform(authenticated(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateBody()), "admin-token"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.data.customerId", is(5)));
    }

    @Test
    void invalidRequestReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":0,\"brand\":\"\",\"model\":\"\","
                                + "\"year\":1800,\"licensePlate\":\"\",\"currentMileage\":-1}"),
                        "admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message", is("Validation failed")));
    }

    @Test
    void missingVehicleReturnsNotFound() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(getVehicleByIdUseCase.getById(99L)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(authenticated(get("/api/v1/vehicles/99"), "admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.message", is("Vehicle not found: 99")));
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private String validCreateBody() {
        return "{\"customerId\":5,\"brand\":\"Nissan\",\"model\":\"Sentra\","
                + "\"year\":2005,\"color\":\"Blue\",\"licensePlate\":\"ABC123\","
                + "\"vin\":\"VIN123\",\"currentMileage\":120000}";
    }

    private String validUpdateBody() {
        return "{\"brand\":\"Nissan\",\"model\":\"Sentra\",\"year\":2005,"
                + "\"color\":\"Blue\",\"licensePlate\":\"ABC123\","
                + "\"vin\":\"VIN123\",\"currentMileage\":120000}";
    }

    private Vehicle vehicle() {
        return new Vehicle(1L, 5L, "Nissan", "Sentra", 2005, "Blue", "ABC123", "VIN123",
                120000, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }
}
