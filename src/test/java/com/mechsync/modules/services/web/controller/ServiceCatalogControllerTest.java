package com.mechsync.modules.services.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import com.mechsync.modules.services.application.port.in.ListServicesUseCase;
import com.mechsync.modules.services.domain.model.ServiceCatalogItem;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value = ServiceCatalogController.class, properties = {
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
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListServicesUseCase listServicesUseCase;

    @Test
    void listWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanListServices() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listServicesUseCase.list(0, 20, null)).thenReturn(page());

        mockMvc.perform(authenticated(get("/api/v1/services"), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Diagnostico")))
                .andExpect(jsonPath("$.data.content[0].basePrice", is(500.0)))
                .andExpect(jsonPath("$.data.content[0].statusId").doesNotExist());
    }

    @Test
    void technicianCanListServicesWithSearch() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listServicesUseCase.list(1, 10, "aceite")).thenReturn(page());

        mockMvc.perform(authenticated(get("/api/v1/services")
                        .param("page", "1").param("size", "10").param("search", "aceite"),
                "technician-token"))
                .andExpect(status().isOk());

        verify(listServicesUseCase).list(1, 10, "aceite");
    }

    @Test
    void customerCannotListServices() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get("/api/v1/services"), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPageSizeReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(get("/api/v1/services").param("size", "0"),
                "admin-token"))
                .andExpect(status().isBadRequest());
    }

    private ServiceCatalogPage page() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        ServiceCatalogItem item = new ServiceCatalogItem(1L, "Diagnostico",
                "Revision electronica", new BigDecimal("500.00"),
                new BigDecimal("1.50"), timestamp, null);
        return new ServiceCatalogPage(List.of(item), 0, 20, 1, 1);
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
}
