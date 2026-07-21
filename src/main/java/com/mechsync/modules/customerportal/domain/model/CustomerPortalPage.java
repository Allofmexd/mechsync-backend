package com.mechsync.modules.customerportal.domain.model;

import java.util.List;

public record CustomerPortalPage<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public CustomerPortalPage {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
