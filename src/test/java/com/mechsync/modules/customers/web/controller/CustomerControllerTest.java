package com.mechsync.modules.customers.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.customers.application.dto.CustomerPage;
import com.mechsync.modules.customers.application.port.in.CreateCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.DeleteCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.GetCustomerByIdUseCase;
import com.mechsync.modules.customers.application.port.in.ListCustomersUseCase;
import com.mechsync.modules.customers.application.port.in.UpdateCustomerUseCase;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
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

@WebMvcTest(value = CustomerController.class, properties = {
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
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

    @MockitoBean
    private GetCustomerByIdUseCase getCustomerByIdUseCase;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private UpdateCustomerUseCase updateCustomerUseCase;

    @MockitoBean
    private DeleteCustomerUseCase deleteCustomerUseCase;

    @Test
    void listWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void technicianCanListCustomers() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(listCustomersUseCase.list(0, 20)).thenReturn(
                new CustomerPage(List.of(customer()), 0, 20, 1, 1));

        mockMvc.perform(authenticated(get("/api/v1/customers"), "technician-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id", is(1)));
    }

    @Test
    void technicianCanGetCustomer() throws Exception {
        tokenRepresents("technician-token", "TECNICO");
        when(getCustomerByIdUseCase.getById(1L)).thenReturn(customer());

        mockMvc.perform(authenticated(get("/api/v1/customers/1"), "technician-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", is(2)));
    }

    @Test
    void customerRoleCannotListCustomers() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get("/api/v1/customers"), "customer-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.message", is("Forbidden")));
    }

    @Test
    void customerRoleCannotCreateCustomer() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2,\"address\":\"Main Street\"}"), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateCustomer() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(createCustomerUseCase.create(any())).thenReturn(customer());

        mockMvc.perform(authenticated(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2,\"address\":\"Main Street\"}"), "admin-token"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/customers/1"))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void invalidCreateRequestReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":0,\"address\":\"   \"}"), "admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message", is("Validation failed")));
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "user@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private Customer customer() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new Customer(1L, 2L, "Main Street", timestamp, timestamp, null);
    }
}
