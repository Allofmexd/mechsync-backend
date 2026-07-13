package com.mechsync.modules.users.application.dto;

import com.mechsync.modules.users.domain.model.User;
import java.util.List;

public record UserPage(
        List<User> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public UserPage {
        content = List.copyOf(content);
    }
}
