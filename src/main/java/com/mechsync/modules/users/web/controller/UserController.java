package com.mechsync.modules.users.web.controller;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.users.application.dto.ChangeUserPasswordCommand;
import com.mechsync.modules.users.application.dto.ChangeUserRoleCommand;
import com.mechsync.modules.users.application.dto.CreateUserCommand;
import com.mechsync.modules.users.application.dto.UpdateUserCommand;
import com.mechsync.modules.users.application.port.in.ChangeUserPasswordUseCase;
import com.mechsync.modules.users.application.port.in.ChangeUserRoleUseCase;
import com.mechsync.modules.users.application.port.in.CreateUserUseCase;
import com.mechsync.modules.users.application.port.in.GetUserByIdUseCase;
import com.mechsync.modules.users.application.port.in.ListUsersUseCase;
import com.mechsync.modules.users.application.port.in.UpdateUserUseCase;
import com.mechsync.modules.users.domain.model.User;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping(ApiPaths.USERS)
public class UserController {

    private final ListUsersUseCase listUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ChangeUserPasswordUseCase changeUserPasswordUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;

    public UserController(
            ListUsersUseCase listUsersUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            CreateUserUseCase createUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            ChangeUserPasswordUseCase changeUserPasswordUseCase,
            ChangeUserRoleUseCase changeUserRoleUseCase) {
        this.listUsersUseCase = listUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.changeUserPasswordUseCase = changeUserPasswordUseCase;
        this.changeUserRoleUseCase = changeUserRoleUseCase;
    }

    @GetMapping
    public ApiResponse<UserPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(UserPageResponse.from(listUsersUseCase.list(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.ok(UserResponse.from(getUserByIdUseCase.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        User created = createUserUseCase.create(new CreateUserCommand(
                request.firstName(), request.lastName(), request.phone(), request.email(),
                request.password(), request.role()));
        return ResponseEntity.created(URI.create(ApiPaths.USERS + "/" + created.id()))
                .body(ApiResponse.ok(UserResponse.from(created)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User updated = updateUserUseCase.update(new UpdateUserCommand(
                id, request.firstName(), request.lastName(), request.phone(), request.email()));
        return ApiResponse.ok(UserResponse.from(updated));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        changeUserPasswordUseCase.changePassword(
                new ChangeUserPasswordCommand(id, request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<UserResponse> changeRole(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal AuthenticatedUser administrator) {
        User updated = changeUserRoleUseCase.changeRole(
                new ChangeUserRoleCommand(id, administrator.id(), request.role()));
        return ApiResponse.ok(UserResponse.from(updated));
    }
}
