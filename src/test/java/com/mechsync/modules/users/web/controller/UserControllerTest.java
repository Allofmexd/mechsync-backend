package com.mechsync.modules.users.web.controller;

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
import com.mechsync.modules.users.application.dto.UserPage;
import com.mechsync.modules.users.application.port.in.ChangeUserPasswordUseCase;
import com.mechsync.modules.users.application.port.in.ChangeUserRoleUseCase;
import com.mechsync.modules.users.application.port.in.CreateUserUseCase;
import com.mechsync.modules.users.application.port.in.GetUserByIdUseCase;
import com.mechsync.modules.users.application.port.in.ListUsersUseCase;
import com.mechsync.modules.users.application.port.in.UpdateUserUseCase;
import com.mechsync.modules.users.domain.exception.DuplicateUserEmailException;
import com.mechsync.modules.users.domain.exception.UserNotFoundException;
import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.domain.model.User;
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

@WebMvcTest(value = UserController.class, properties = {
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
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private ListUsersUseCase listUsersUseCase;
    @MockitoBean private GetUserByIdUseCase getUserByIdUseCase;
    @MockitoBean private CreateUserUseCase createUserUseCase;
    @MockitoBean private UpdateUserUseCase updateUserUseCase;
    @MockitoBean private ChangeUserPasswordUseCase changeUserPasswordUseCase;
    @MockitoBean private ChangeUserRoleUseCase changeUserRoleUseCase;

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void technicianCannotAccessUsers() throws Exception {
        tokenRepresents("technician-token", "TECNICO");

        mockMvc.perform(authenticated(get("/api/v1/users"), "technician-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotAccessUsers() throws Exception {
        tokenRepresents("customer-token", "CLIENTE");

        mockMvc.perform(authenticated(get("/api/v1/users"), "customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanListUsers() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(listUsersUseCase.list(0, 20)).thenReturn(
                new UserPage(List.of(user()), 0, 20, 1, 1));

        mockMvc.perform(authenticated(get("/api/v1/users"), "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email", is("juan@example.com")))
                .andExpect(jsonPath("$.data.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].passwordHash").doesNotExist());
    }

    @Test
    void administratorCanCreateUser() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(createUserUseCase.create(any())).thenReturn(user());

        mockMvc.perform(authenticated(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()), "admin-token"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/2"))
                .andExpect(jsonPath("$.data.roles[0]", is("CLIENTE")))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void invalidCreateRequestReturnsBadRequest() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");

        mockMvc.perform(authenticated(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"\",\"email\":\"invalid\","
                                + "\"password\":\"short\",\"role\":\"UNKNOWN\"}"), "admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message", is("Validation failed")));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(createUserUseCase.create(any()))
                .thenThrow(new DuplicateUserEmailException("juan@example.com"));

        mockMvc.perform(authenticated(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()), "admin-token"))
                .andExpect(status().isConflict());
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        tokenRepresents("admin-token", "ADMINISTRADOR");
        when(getUserByIdUseCase.getById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(authenticated(get("/api/v1/users/99"), "admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.message", is("User not found: 99")));
    }

    private void tokenRepresents(String token, String role) {
        when(jwtService.parse(token)).thenReturn(
                new AuthenticatedUser(1L, "admin@mechsync.local", Set.of(role)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private String validCreateBody() {
        return "{\"firstName\":\"Juan\",\"lastName\":\"Perez\","
                + "\"phone\":\"9610000000\",\"email\":\"juan@example.com\","
                + "\"password\":\"Password123!\",\"role\":\"CLIENTE\"}";
    }

    private User user() {
        return new User(2L, "Juan", "Perez", "9610000000", "juan@example.com",
                "internal-hash", Set.of(new Role(3L, "CLIENTE")),
                LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }
}
