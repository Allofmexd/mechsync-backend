package com.mechsync.modules.jobs.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.jobs.application.dto.JobPage;
import com.mechsync.modules.jobs.application.port.in.*;
import com.mechsync.modules.jobs.domain.model.*;
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

@WebMvcTest(value = JobController.class, properties = {"debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class JobControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwt;
    @MockitoBean JobQueryUseCase query;
    @MockitoBean CreateJobUseCase create;
    @MockitoBean JobWorkflowUseCase workflow;

    @Test void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/jobs")).andExpect(status().isUnauthorized());
    }

    @Test void clientAndTechnicianAreForbidden() throws Exception {
        token("client", "CLIENTE");
        token("tech", "TECNICO");
        mvc.perform(auth(get("/api/v1/jobs"), "client")).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/jobs"), "tech")).andExpect(status().isForbidden());
        mvc.perform(auth(patch("/api/v1/jobs/1/start"), "tech"))
                .andExpect(status().isForbidden());
    }

    @Test void administratorCanUseAllEndpoints() throws Exception {
        token("admin", "ADMINISTRADOR");
        Job job = job();
        when(query.list(0, 20)).thenReturn(new JobPage(List.of(job), 0, 20, 1, 1));
        when(query.get(1L)).thenReturn(job);
        when(create.create(any())).thenReturn(job);
        when(workflow.start(1L)).thenReturn(job);
        when(workflow.complete(any())).thenReturn(job);
        when(workflow.cancel(any())).thenReturn(job);

        mvc.perform(auth(get("/api/v1/jobs"), "admin")).andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/jobs/1"), "admin")).andExpect(status().isOk());
        mvc.perform(auth(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON)
                .content("{\"workOrderId\":1,\"initialApprovedRevisionId\":7,\"technicianId\":3}"),
                "admin")).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/jobs/1"));
        mvc.perform(auth(patch("/api/v1/jobs/1/start"), "admin")).andExpect(status().isOk());
        mvc.perform(auth(patch("/api/v1/jobs/1/complete").contentType(MediaType.APPLICATION_JSON)
                .content("{\"realSubtotalAmount\":100.00,\"realIvaAmount\":16.00,"
                        + "\"realTotalAmount\":116.00}"), "admin"))
                .andExpect(status().isOk());
        mvc.perform(auth(patch("/api/v1/jobs/1/cancel").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancellationNotes\":\"Customer decision\"}"), "admin"))
                .andExpect(status().isOk());
    }

    @Test void invalidMoneyPayloadIs400() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(patch("/api/v1/jobs/1/complete").contentType(MediaType.APPLICATION_JSON)
                .content("{\"realSubtotalAmount\":-1,\"realIvaAmount\":0,"
                        + "\"realTotalAmount\":0}"), "admin"))
                .andExpect(status().isBadRequest());
    }

    @Test void apiV2RouteDoesNotExist() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(get("/api/v2/jobs"), "admin")).andExpect(status().isNotFound());
    }

    private void token(String value, String role) {
        when(jwt.parse(value)).thenReturn(
                new AuthenticatedUser(99L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private Job job() {
        return new Job(1L, 1L, 7L, 3L, JobStatus.PENDIENTE, null, null, null, null,
                null, new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), "Notes", null, LocalDateTime.now(), null);
    }
}
