package com.mechsync.modules.technicians.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
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
                LocalDate.of(2025, 1, 15));
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
