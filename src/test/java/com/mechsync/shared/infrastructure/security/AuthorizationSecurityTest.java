package com.mechsync.shared.infrastructure.security;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = TestSecuredController.class, properties = {
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
class AuthorizationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void administratorCanAccessAdministratorEndpoint() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticatedGet("/test/admin-only", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is("admin")));
    }

    @Test
    void technicianCannotAccessAdministratorEndpoint() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticatedGet("/test/admin-only", "technician-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is("Forbidden")));
    }

    @Test
    void technicianCanAccessTechnicianEndpoint() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticatedGet("/test/technician-only", "technician-token"))
                .andExpect(status().isOk());
    }

    @Test
    void customerCanAccessCustomerEndpoint() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticatedGet("/test/customer-only", "customer-token"))
                .andExpect(status().isOk());
    }

    @Test
    void anyAuthenticatedRoleCanAccessAuthenticatedEndpoint() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticatedGet("/test/authenticated-only", "customer-token"))
                .andExpect(status().isOk());
    }

    @Test
    void missingTokenReturnsUnauthorizedBeforeMethodAuthorization() throws Exception {
        mockMvc.perform(get("/test/admin-only"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message", is("Unauthorized")));
    }

    @Test
    void prefixedRoleClaimIsRejectedInsteadOfBeingDoublePrefixed() throws Exception {
        tokenRepresents("prefixed-token", "ROLE_ADMINISTRADOR");

        mockMvc.perform(authenticatedGet("/test/admin-only", "prefixed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message", is("Unauthorized")));
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(
            String path, String token) {
        return get(path).header("Authorization", "Bearer " + token);
    }
}
