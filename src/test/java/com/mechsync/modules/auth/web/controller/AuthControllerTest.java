package com.mechsync.modules.auth.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.application.dto.LoginResult;
import com.mechsync.modules.auth.application.port.in.LoginUseCase;
import com.mechsync.modules.auth.domain.exception.InvalidCredentialsException;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class, properties = {
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void loginIsPublicAndReturnsToken() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(
                1L, "admin@example.com", Set.of("ADMINISTRADOR"));
        when(loginUseCase.login(any())).thenReturn(
                new LoginResult(new GeneratedToken("jwt", 7200), user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"local-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", is("jwt")))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.roles[0]", is("ADMINISTRADOR")));
    }

    @Test
    void invalidCredentialsReturnUnauthorized() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://mechsync-frontend.vercel.app")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer stale-login-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://mechsync-frontend.vercel.app"))
                .andExpect(jsonPath("$.data.message", is("Invalid credentials")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message", is("Unauthorized")));
    }

    @Test
    void meWithInvalidTokenReturnsUnauthorized() throws Exception {
        when(jwtService.parse("invalid-token")).thenThrow(new io.jsonwebtoken.MalformedJwtException(
                "invalid"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message", is("Unauthorized")));
    }
}
