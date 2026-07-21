package com.mechsync.modules.customers.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.auth.infrastructure.security.SpringSecurityCurrentAuthenticatedUserAdapter;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.application.usecase.AuthenticatedCustomerService;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = TestCustomerPortalSecurityController.class, properties = {
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
        GlobalExceptionHandler.class,
        SpringSecurityCurrentAuthenticatedUserAdapter.class,
        AuthenticatedCustomerService.class
})
class CustomerPortalSecurityTest {

    private static final String PATH = "/api/v1/test/customer-portal/me";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomerRepositoryPort customerRepository;

    @Test
    void customerWithProfileCanResolveIdentity() throws Exception {
        tokenRepresents("customer-token", 7L, "CLIENTE");
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.of(customer()));

        mockMvc.perform(authenticatedGet("customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId", is(3)));
    }

    @Test
    void customerWithoutProfileReceivesControlledForbidden() throws Exception {
        tokenRepresents("customer-token", 7L, "CLIENTE");
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.empty());

        mockMvc.perform(authenticatedGet("customer-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is(
                        "Tu cuenta no tiene un perfil de cliente asociado. Contacta al taller.")));
    }

    @Test
    void technicianCannotEnterCustomerPortalResolver() throws Exception {
        tokenRepresents("technician-token", 7L, "TECNICO");

        mockMvc.perform(authenticatedGet("technician-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is("Forbidden")));
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void administratorCannotEnterCustomerPortalResolver() throws Exception {
        tokenRepresents("admin-token", 1L, "ADMINISTRADOR");

        mockMvc.perform(authenticatedGet("admin-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is("Forbidden")));
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void anonymousRequestIsRejectedBySpringSecurity() throws Exception {
        mockMvc.perform(get(PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message", is("Unauthorized")));
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void inconsistentCustomerRelationshipReturnsControlledServerError() throws Exception {
        tokenRepresents("customer-token", 7L, "CLIENTE");
        when(customerRepository.findByUserId(7L)).thenThrow(new CustomerIntegrityException());

        mockMvc.perform(authenticatedGet("customer-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.data.message", is(
                        "No fue posible resolver el perfil de cliente.")));
    }

    private void tokenRepresents(String token, Long userId, String role) {
        when(jwtService.parse(token)).thenReturn(new AuthenticatedUser(
                userId, "user@mechsync.local", Set.of(role)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(
            String token) {
        return get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private Customer customer() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 21, 12, 0);
        return new Customer(3L, 7L, null, timestamp, timestamp, null);
    }
}
