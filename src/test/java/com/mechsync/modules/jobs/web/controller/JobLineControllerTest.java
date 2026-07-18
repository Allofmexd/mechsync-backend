package com.mechsync.modules.jobs.web.controller;

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
import com.mechsync.modules.jobs.application.port.in.JobLineUseCase;
import com.mechsync.modules.jobs.domain.exception.JobConflictException;
import com.mechsync.modules.jobs.domain.exception.JobLineNotFoundException;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value = JobLineController.class, properties = {"debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class JobLineControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwt;
    @MockitoBean JobLineUseCase useCase;

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/jobs/1/services")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/jobs/1/parts")).andExpect(status().isUnauthorized());
    }

    @Test
    void clientAndTechnicianAreForbidden() throws Exception {
        token("client", "CLIENTE");
        token("tech", "TECNICO");

        mvc.perform(auth(get("/api/v1/jobs/1/services"), "client"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/v1/jobs/1/parts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(partJson()), "tech")).andExpect(status().isForbidden());
    }

    @Test
    void administratorCanUseServiceEndpoints() throws Exception {
        token("admin", "ADMINISTRADOR");
        JobServiceLine line = serviceLine();
        when(useCase.listServices(1L)).thenReturn(List.of(line));
        when(useCase.addService(any())).thenReturn(line);
        when(useCase.updateService(any())).thenReturn(line);

        mvc.perform(auth(get("/api/v1/jobs/1/services"), "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].lineSubtotal").value(1200.00));
        mvc.perform(auth(post("/api/v1/jobs/1/services")
                .contentType(MediaType.APPLICATION_JSON).content(serviceJson()), "admin"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/jobs/1/services/10"));
        mvc.perform(auth(put("/api/v1/jobs/1/services/10")
                .contentType(MediaType.APPLICATION_JSON).content(serviceJson()), "admin"))
                .andExpect(status().isOk());
        mvc.perform(auth(delete("/api/v1/jobs/1/services/10"), "admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    void administratorCanUsePartEndpoints() throws Exception {
        token("admin", "ADMINISTRADOR");
        JobPartLine line = partLine();
        when(useCase.listParts(1L)).thenReturn(List.of(line));
        when(useCase.addPart(any())).thenReturn(line);
        when(useCase.updatePart(any())).thenReturn(line);

        mvc.perform(auth(get("/api/v1/jobs/1/parts"), "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].lineSubtotal").value(800.00));
        mvc.perform(auth(post("/api/v1/jobs/1/parts")
                .contentType(MediaType.APPLICATION_JSON).content(partJson()), "admin"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/jobs/1/parts/20"));
        mvc.perform(auth(put("/api/v1/jobs/1/parts/20")
                .contentType(MediaType.APPLICATION_JSON).content(partJson()), "admin"))
                .andExpect(status().isOk());
        mvc.perform(auth(delete("/api/v1/jobs/1/parts/20"), "admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidLinePayloadIs400() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(post("/api/v1/jobs/1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceId\":2,\"quantity\":0,\"unitPrice\":-1}"), "admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongJobLineIs404AndTerminalMutationIs409() throws Exception {
        token("admin", "ADMINISTRADOR");
        when(useCase.updateService(any())).thenThrow(
                new JobLineNotFoundException("Service", 10L, 1L));
        when(useCase.addPart(any())).thenThrow(
                new JobConflictException("Job lines cannot change in terminal status"));

        mvc.perform(auth(put("/api/v1/jobs/1/services/10")
                .contentType(MediaType.APPLICATION_JSON).content(serviceJson()), "admin"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/v1/jobs/1/parts")
                .contentType(MediaType.APPLICATION_JSON).content(partJson()), "admin"))
                .andExpect(status().isConflict());
    }

    @Test
    void apiV2RoutesDoNotExist() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(auth(get("/api/v2/jobs/1/services"), "admin"))
                .andExpect(status().isNotFound());
    }

    private void token(String value, String role) {
        when(jwt.parse(value)).thenReturn(
                new AuthenticatedUser(99L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String value) {
        return request.header("Authorization", "Bearer " + value);
    }

    private String serviceJson() {
        return "{\"serviceId\":2,\"quantity\":1.00,\"unitPrice\":1200.00}";
    }

    private String partJson() {
        return "{\"partId\":3,\"quantity\":1.00,\"unitPrice\":800.00}";
    }

    private JobServiceLine serviceLine() {
        return new JobServiceLine(10L, 1L, 2L, "Transmission service", BigDecimal.ONE,
                new BigDecimal("1200.00"), new BigDecimal("1200.00"),
                LocalDateTime.now(), null);
    }

    private JobPartLine partLine() {
        return new JobPartLine(20L, 1L, 3L, "Transmission filter", BigDecimal.ONE,
                new BigDecimal("800.00"), new BigDecimal("800.00"),
                LocalDateTime.now(), null);
    }
}
