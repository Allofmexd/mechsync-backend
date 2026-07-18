package com.mechsync.modules.technicians.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.technicians.application.dto.CreateTechnicianCommand;
import com.mechsync.modules.technicians.application.dto.UpdateTechnicianCommand;
import com.mechsync.modules.technicians.application.port.in.CreateTechnicianUseCase;
import com.mechsync.modules.technicians.application.port.in.GetTechnicianByIdUseCase;
import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
import com.mechsync.modules.technicians.application.port.in.UpdateTechnicianUseCase;
import com.mechsync.modules.technicians.domain.exception.TechnicianNotFoundException;
import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.time.LocalDate;
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

@WebMvcTest(value = TechnicianController.class, properties = {
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
class TechnicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListTechniciansUseCase listTechniciansUseCase;

    @MockitoBean
    private GetTechnicianByIdUseCase getTechnicianByIdUseCase;

    @MockitoBean
    private CreateTechnicianUseCase createTechnicianUseCase;

    @MockitoBean
    private UpdateTechnicianUseCase updateTechnicianUseCase;

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(path())).andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanListTechniciansWithoutSensitiveFields() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listTechniciansUseCase.list()).thenReturn(List.of(technician()));

        mockMvc.perform(authenticated(get(path()), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].userId", is(3)))
                .andExpect(jsonPath("$.data[0].fullName", is("Ana Torres")))
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    @Test
    void technicianCanListTechnicians() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listTechniciansUseCase.list()).thenReturn(List.of(technician()));

        mockMvc.perform(authenticated(get(path()), "technician-token"))
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotListTechnicians() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get(path()), "customer-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is("Forbidden")));
    }

    @Test
    void administratorCanGetTechnician() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(getTechnicianByIdUseCase.getById(1L)).thenReturn(technician());

        mockMvc.perform(authenticated(get(path() + "/1"), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.userId", is(3)))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void missingTechnicianReturnsNotFound() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(getTechnicianByIdUseCase.getById(99L))
                .thenThrow(new TechnicianNotFoundException(99L));

        mockMvc.perform(authenticated(get(path() + "/99"), "admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.message", is("Technician not found: 99")));
    }

    @Test
    void technicianCannotGetTechnicianDetail() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticated(get(path() + "/1"), "technician-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateTechnician() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(createTechnicianUseCase.create(new CreateTechnicianCommand(
                3L, 1L, LocalDate.of(2025, 1, 15))))
                .thenReturn(technician());

        mockMvc.perform(authenticated(post(path()), "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 3,
                                  "specialtyId": 1,
                                  "hireDate": "2025-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", path() + "/1"))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void technicianCannotCreateTechnicianProfile() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticated(post(path()), "technician-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":3,\"specialtyId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotCreateTechnicianProfile() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(post(path()), "customer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":3,\"specialtyId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanUpdateTechnicianWithoutChangingUser() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(updateTechnicianUseCase.update(new UpdateTechnicianCommand(
                1L, 2L, LocalDate.of(2025, 1, 15))))
                .thenReturn(new Technician(
                        1L, 3L, "Ana", "Torres", "ana@example.com", "9610000000",
                        2L, "DIAGNOSTICO_ELECTRONICO", LocalDate.of(2025, 1, 15),
                        null, null));

        mockMvc.perform(authenticated(put(path() + "/1"), "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "specialtyId": 2,
                                  "hireDate": "2025-01-15"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", is(3)))
                .andExpect(jsonPath("$.data.specialtyId", is(2)));
    }

    @Test
    void technicianCannotUpdateTechnicianProfile() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticated(put(path() + "/1"), "technician-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"specialtyId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpointDoesNotExist() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(delete(path() + "/1"), "admin-token"))
                .andExpect(status().isMethodNotAllowed());
    }

    private Technician technician() {
        return new Technician(
                1L,
                3L,
                "Ana",
                "Torres",
                "ana@example.com",
                "9610000000",
                1L,
                "TRANSMISIONES_AUTOMATICAS",
                LocalDate.of(2025, 1, 15),
                null,
                null);
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private String path() {
        return "/api/v1/technicians";
    }
}
