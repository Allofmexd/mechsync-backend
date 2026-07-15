package com.mechsync.modules.catalogs.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.catalogs.application.port.in.ListCatalogStatusesUseCase;
import com.mechsync.modules.catalogs.domain.exception.InvalidStatusContextException;
import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import com.mechsync.modules.catalogs.domain.model.StatusContext;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value = CatalogController.class, properties = {
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
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListCatalogStatusesUseCase listCatalogStatusesUseCase;

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(path()).param("context", "VEHICLE_INTAKES"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanListOnlyRequestedContext() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listCatalogStatusesUseCase.listByContext("VEHICLE_INTAKES"))
                .thenReturn(List.of(statusEntry()));

        mockMvc.perform(authenticated(
                        get(path()).param("context", "VEHICLE_INTAKES"), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(7)))
                .andExpect(jsonPath("$.data[0].code", is("EN_DIAGNOSTICO")))
                .andExpect(jsonPath("$.data[0].context", is("VEHICLE_INTAKES")));
    }

    @Test
    void technicianCanListStatuses() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listCatalogStatusesUseCase.listByContext("VEHICLE_INTAKES"))
                .thenReturn(List.of(statusEntry()));

        mockMvc.perform(authenticated(
                        get(path()).param("context", "VEHICLE_INTAKES"), "technician-token"))
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotListStatuses() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(
                        get(path()).param("context", "VEHICLE_INTAKES"), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidContextReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listCatalogStatusesUseCase.listByContext("INVALID"))
                .thenThrow(new InvalidStatusContextException("INVALID"));

        mockMvc.perform(authenticated(
                        get(path()).param("context", "INVALID"), "admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message", is("Invalid status context: INVALID")));
    }

    private CatalogStatus statusEntry() {
        return new CatalogStatus(
                7L,
                StatusContext.VEHICLE_INTAKES,
                "EN_DIAGNOSTICO",
                "Vehiculo en revision inicial y diagnostico.");
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
        return "/api/v1/catalogs/statuses";
    }
}
