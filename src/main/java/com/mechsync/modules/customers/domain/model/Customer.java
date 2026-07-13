package com.mechsync.modules.customers.domain.model;

import java.time.LocalDateTime;

public record Customer(
        Long id,
        Long userId,
        String address,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
