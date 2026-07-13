package com.mechsync.modules.users.web.controller;

import com.mechsync.modules.users.application.dto.UserPage;
import java.util.List;

public record UserPageResponse(
        List<UserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public UserPageResponse {
        content = List.copyOf(content);
    }

    public static UserPageResponse from(UserPage result) {
        return new UserPageResponse(
                result.content().stream().map(UserResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
