package com.mechsync.modules.servicereports.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.servicereports.application.dto.ServiceReportPage;
import com.mechsync.modules.servicereports.application.port.in.*;
import com.mechsync.modules.servicereports.domain.model.*;
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

@WebMvcTest(value = ServiceReportController.class, properties = {"debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class ServiceReportControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwt;
    @MockitoBean ServiceReportQueryUseCase query;
    @MockitoBean CreateServiceReportUseCase create;

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/service-reports")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/jobs/1/service-report")).andExpect(status().isUnauthorized());
    }

    @Test
    void clientAndTechnicianAreForbidden() throws Exception {
        token("client", "CLIENTE");
        token("tech", "TECNICO");
        mvc.perform(auth(get("/api/v1/service-reports"), "client"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/service-reports"), "tech"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/v1/service-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":1,\"finalDescription\":\"Done\"}"), "tech"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanListGetByIdGetByJobAndCreate() throws Exception {
        token("admin", "ADMINISTRADOR");
        ServiceReport report = report();
        when(query.list(0, 20)).thenReturn(new ServiceReportPage(List.of(report), 0, 20, 1, 1));
        when(query.get(9L)).thenReturn(report);
        when(query.getByJobId(1L)).thenReturn(report);
        when(create.create(any())).thenReturn(report);

        mvc.perform(auth(get("/api/v1/service-reports"), "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].finalTotal").value(4245.60));
        mvc.perform(auth(get("/api/v1/service-reports/9"), "admin"))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/jobs/1/service-report"), "admin"))
                .andExpect(status().isOk());
        mvc.perform(auth(post("/api/v1/service-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":1,\"finalDescription\":\"Trabajo completado\","
                        + "\"customerConfirmation\":true}"), "admin"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/service-reports/9"));
    }

    @Test
    void invalidRequestIs400AndDeleteIs405() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(post("/api/v1/service-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":1,\"finalDescription\":\" \"}"), "admin"))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(delete("/api/v1/service-reports/1"), "admin"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void apiV2RouteDoesNotExist() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(get("/api/v2/service-reports"), "admin"))
                .andExpect(status().isNotFound());
    }

    private void token(String value, String role) {
        when(jwt.parse(value)).thenReturn(
                new AuthenticatedUser(99L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private ServiceReport report() {
        LocalDateTime now = LocalDateTime.now();
        return new ServiceReport(9L, 1L, ServiceReportStatus.COMPLETADO, now,
                "Trabajo completado", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"), true,
                null, now, null);
    }
}
