package com.mechsync.modules.parts.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import com.mechsync.modules.parts.application.port.in.ListPartsUseCase;
import com.mechsync.modules.parts.domain.model.PartCatalogItem;
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

@WebMvcTest(value = PartCatalogController.class, properties = {
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
class PartCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListPartsUseCase listPartsUseCase;

    @Test
    void listWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/parts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanListParts() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listPartsUseCase.list(0, 20, null)).thenReturn(page());

        mockMvc.perform(authenticated(get("/api/v1/parts"), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Filtro")))
                .andExpect(jsonPath("$.data.content[0].unitPrice", is(800.0)))
                .andExpect(jsonPath("$.data.content[0].measurementUnitName", is("PIEZA")))
                .andExpect(jsonPath("$.data.content[0].availableStock").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].statusId").doesNotExist());
    }

    @Test
    void technicianCanListPartsWithSearch() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listPartsUseCase.list(1, 10, "filtro")).thenReturn(page());

        mockMvc.perform(authenticated(get("/api/v1/parts")
                        .param("page", "1").param("size", "10").param("search", "filtro"),
                "technician-token"))
                .andExpect(status().isOk());

        verify(listPartsUseCase).list(1, 10, "filtro");
    }

    @Test
    void customerCannotListParts() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get("/api/v1/parts"), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPageSizeReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(get("/api/v1/parts").param("size", "101"),
                "admin-token"))
                .andExpect(status().isBadRequest());
    }

    private PartCatalogPage page() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        PartCatalogItem item = new PartCatalogItem(1L, "Filtro", "Filtro compatible",
                new BigDecimal("800.00"), 1L, "PIEZA", "PZA", timestamp, null);
        return new PartCatalogPage(List.of(item), 0, 20, 1, 1);
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
