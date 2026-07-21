package com.mechsync.modules.specialties.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.specialties.application.port.in.ListSpecialtiesUseCase;
import com.mechsync.modules.specialties.domain.model.Specialty;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = SpecialtyController.class, properties = {
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
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListSpecialtiesUseCase listSpecialtiesUseCase;

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(path())).andExpect(status().isUnauthorized());
    }

    @Test
    void administratorReceivesRealCatalog() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listSpecialtiesUseCase.list()).thenReturn(List.of(
                new Specialty(2L, "DIAGNOSTICO_ELECTRONICO", "Diagnóstico electrónico")));

        mockMvc.perform(get(path()).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(2)))
                .andExpect(jsonPath("$.data[0].code", is("DIAGNOSTICO_ELECTRONICO")))
                .andExpect(jsonPath("$.data[0].name", is("Diagnostico electronico")))
                .andExpect(jsonPath("$.data[0].description", is("Diagnóstico electrónico")));
    }

    @Test
    void emptyCatalogReturnsOkAndEmptyList() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listSpecialtiesUseCase.list()).thenReturn(List.of());

        mockMvc.perform(get(path()).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void technicianAndCustomerAreForbidden() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(get(path()).header("Authorization", "Bearer technician-token"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(path()).header("Authorization", "Bearer customer-token"))
                .andExpect(status().isForbidden());
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private String path() {
        return "/api/v1/specialties";
    }
}
