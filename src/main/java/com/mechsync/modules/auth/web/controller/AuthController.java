package com.mechsync.modules.auth.web.controller;

import com.mechsync.modules.auth.application.dto.LoginCommand;
import com.mechsync.modules.auth.application.dto.LoginResult;
import com.mechsync.modules.auth.application.port.in.LoginUseCase;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(
                new LoginCommand(request.email(), request.password()));
        LoginResponse response = new LoginResponse(
                result.token().value(),
                "Bearer",
                result.token().expiresInSeconds(),
                AuthUserResponse.from(result.user()));
        return ApiResponse.ok(response);
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> me(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ApiResponse.ok(AuthUserResponse.from(authenticatedUser));
    }
}
