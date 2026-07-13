package com.mechsync.shared.web.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.shared.application.health.DatabaseHealthChecker;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = HealthController.class, properties = {
        "debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatabaseHealthChecker databaseHealthChecker;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void returnsHealthStatusWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.application", is("mechsync-backend")))
                .andExpect(jsonPath("$.data.timestamp", notNullValue()));
    }

    @Test
    void returnsDatabaseUpWithoutAuthentication() throws Exception {
        when(databaseHealthChecker.isAvailable()).thenReturn(true);

        mockMvc.perform(get("/api/v1/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.timestamp", notNullValue()));
    }

    @Test
    void returnsServiceUnavailableWhenDatabaseIsDown() throws Exception {
        when(databaseHealthChecker.isAvailable()).thenReturn(false);

        mockMvc.perform(get("/api/v1/health/database"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.data.status", is("DOWN")))
                .andExpect(jsonPath("$.data.timestamp", notNullValue()));
    }
}
