package com.mechsync.modules.servicereports.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.servicereports.application.dto.ServiceReportPage;
import com.mechsync.modules.servicereports.application.dto.GeneratedServiceReportPdf;
import com.mechsync.modules.servicereports.application.port.in.*;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportNotFoundException;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportPdfGenerationException;
import com.mechsync.modules.servicereports.domain.model.*;
import com.mechsync.modules.technicians.application.port.in.ResolveAuthenticatedTechnicianUseCase;
import com.mechsync.modules.technicians.domain.exception.TechnicianProfileRequiredException;
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
    @MockitoBean GenerateServiceReportPdfUseCase pdf;
    @MockitoBean ResolveAuthenticatedTechnicianUseCase technicianResolver;

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/service-reports")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/service-reports/assigned-to-me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/jobs/1/service-report")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/service-reports/1/pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientIsForbiddenAndTechnicianCannotUseGlobalListOrCreate() throws Exception {
        token("client", "CLIENTE");
        token("tech", "TECNICO");
        mvc.perform(auth(get("/api/v1/service-reports"), "client"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/service-reports/assigned-to-me"), "client"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/service-reports"), "tech"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/service-reports/1/pdf"), "client"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/v1/service-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":1,\"finalDescription\":\"Done\"}"), "tech"))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianWithoutProfileGetsControlled403() throws Exception {
        token("tech", "TECNICO");
        when(technicianResolver.resolveId(any()))
                .thenThrow(new TechnicianProfileRequiredException());

        mvc.perform(auth(get("/api/v1/service-reports/assigned-to-me"), "tech"))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCanReadAndDownloadOnlyAssignedReports() throws Exception {
        token("tech", "TECNICO");
        ServiceReport report = report();
        byte[] content = "%PDF-1.7 assigned".getBytes(
                java.nio.charset.StandardCharsets.US_ASCII);
        when(technicianResolver.resolveId(any())).thenReturn(3L);
        when(query.listAssignedTo(3L, 0, 20))
                .thenReturn(new ServiceReportPage(List.of(report), 0, 20, 1, 1));
        when(query.getAssignedTo(9L, 3L)).thenReturn(report);
        when(query.getByJobIdAssignedTo(1L, 3L)).thenReturn(report);
        when(pdf.generateAssignedTo(9L, 3L)).thenReturn(
                new GeneratedServiceReportPdf("service-report-9.pdf", content));
        when(query.getAssignedTo(99L, 3L))
                .thenThrow(new ServiceReportNotFoundException(99L));
        when(pdf.generateAssignedTo(99L, 3L))
                .thenThrow(new ServiceReportNotFoundException(99L));

        mvc.perform(auth(get("/api/v1/service-reports/assigned-to-me"), "tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(auth(get("/api/v1/service-reports/9"), "tech"))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/jobs/1/service-report"), "tech"))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/service-reports/9/pdf"), "tech"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mvc.perform(auth(get("/api/v1/service-reports/99"), "tech"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/v1/service-reports/99/pdf"), "tech"))
                .andExpect(status().isNotFound());
    }

    @Test
    void administratorCanDownloadPdfWithControlledHeaders() throws Exception {
        token("admin", "ADMINISTRADOR");
        byte[] content = "%PDF-1.7 test".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(pdf.generate(9L)).thenReturn(
                new GeneratedServiceReportPdf("service-report-9.pdf", content));

        mvc.perform(auth(get("/api/v1/service-reports/9/pdf"), "admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"service-report-9.pdf\""))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(content));
    }

    @Test
    void missingReportPdfIs404() throws Exception {
        token("admin", "ADMINISTRADOR");
        when(pdf.generate(999L)).thenThrow(new ServiceReportNotFoundException(999L));

        mvc.perform(auth(get("/api/v1/service-reports/999/pdf"), "admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void pdfGenerationFailureIsControlled500() throws Exception {
        token("admin", "ADMINISTRADOR");
        when(pdf.generate(9L)).thenThrow(
                new ServiceReportPdfGenerationException("generation failed"));

        mvc.perform(auth(get("/api/v1/service-reports/9/pdf"), "admin"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.data.message").value("Unexpected error"));
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
        org.mockito.Mockito.verifyNoInteractions(technicianResolver);
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
