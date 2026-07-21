package com.mechsync.modules.dashboard.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.dashboard.application.port.in.DashboardQueryUseCase;
import com.mechsync.modules.dashboard.application.usecase.DashboardPeriodResolver;
import com.mechsync.modules.dashboard.domain.exception.InvalidDashboardPeriodException;
import com.mechsync.modules.dashboard.domain.model.*;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.*;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = DashboardController.class, properties = {
        "debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class})
class DashboardControllerTest {
    private static final String BASE = "/api/v1/dashboard";
    private static final DashboardPeriod PERIOD = new DashboardPeriod(
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 21));

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtService jwt;
    @MockitoBean
    private DashboardQueryUseCase dashboard;
    @MockitoBean
    private DashboardPeriodResolver periods;

    @BeforeEach
    void setUp() {
        when(periods.resolve(any(), any())).thenReturn(PERIOD);
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get(BASE + "/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void administratorReceivesAllAggregates() throws Exception {
        token("admin", "ADMINISTRADOR");
        when(dashboard.summary(PERIOD)).thenReturn(new DashboardSummary(
                10, 20, 3, 4, 2, new BigDecimal("1500.00"), "MXN",
                PERIOD.from(), PERIOD.to()));
        when(dashboard.workOrdersByStatus(PERIOD)).thenReturn(List.of(
                new StatusMetric("PENDIENTE", "Pendiente", 3)));
        when(dashboard.jobsByStatus(PERIOD)).thenReturn(List.of());
        when(dashboard.revenueByMonth(PERIOD)).thenReturn(List.of());
        when(dashboard.topServices(PERIOD, 5)).thenReturn(List.of());
        when(dashboard.technicianWorkload(PERIOD)).thenReturn(List.of());

        mvc.perform(authorized(BASE + "/summary", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registeredCustomers").value(10))
                .andExpect(jsonPath("$.data.periodRevenue").value(1500.00))
                .andExpect(jsonPath("$.data.currency").value("MXN"));
        mvc.perform(authorized(BASE + "/work-orders-by-status", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].statusCode").value("PENDIENTE"));
        mvc.perform(authorized(BASE + "/jobs-by-status", "admin")).andExpect(status().isOk());
        mvc.perform(authorized(BASE + "/revenue-by-month", "admin")).andExpect(status().isOk());
        mvc.perform(authorized(BASE + "/top-services", "admin")).andExpect(status().isOk());
        mvc.perform(authorized(BASE + "/technician-workload", "admin"))
                .andExpect(status().isOk());
    }

    @Test
    void technicianAndCustomerAreForbiddenFromEveryEndpoint() throws Exception {
        token("tech", "TECNICO");
        token("customer", "CLIENTE");
        String[] endpoints = {"summary", "work-orders-by-status", "jobs-by-status",
                "revenue-by-month", "top-services", "technician-workload"};
        for (String endpoint : endpoints) {
            mvc.perform(authorized(BASE + "/" + endpoint, "tech"))
                    .andExpect(status().isForbidden());
            mvc.perform(authorized(BASE + "/" + endpoint, "customer"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void invalidFormatRangeAndLimitAreBadRequests() throws Exception {
        token("admin", "ADMINISTRADOR");
        mvc.perform(authorized(BASE + "/summary?from=invalid", "admin"))
                .andExpect(status().isBadRequest());

        when(periods.resolve(LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 21)))
                .thenThrow(new InvalidDashboardPeriodException("Invalid range"));
        mvc.perform(authorized(BASE + "/summary?from=2026-07-22&to=2026-07-21", "admin"))
                .andExpect(status().isBadRequest());
        mvc.perform(authorized(BASE + "/top-services?limit=0", "admin"))
                .andExpect(status().isBadRequest());
    }

    private void token(String value, String role) {
        when(jwt.parse(value)).thenReturn(new AuthenticatedUser(
                1L, "user@mechsync.local", Set.of(role)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorized(
            String path, String token) {
        return get(path).header("Authorization", "Bearer " + token);
    }
}
