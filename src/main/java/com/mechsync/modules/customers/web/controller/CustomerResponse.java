package com.mechsync.modules.customers.web.controller;

import com.mechsync.modules.customers.domain.model.Customer;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        Long userId,
        String address,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.id(), customer.userId(), customer.address(), customer.registeredAt(),
                customer.createdAt(), customer.updatedAt());
    }
}
