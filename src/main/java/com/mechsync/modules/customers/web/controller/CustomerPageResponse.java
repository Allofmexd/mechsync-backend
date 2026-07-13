package com.mechsync.modules.customers.web.controller;

import com.mechsync.modules.customers.application.dto.CustomerPage;
import java.util.List;

public record CustomerPageResponse(
        List<CustomerResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public CustomerPageResponse {
        content = List.copyOf(content);
    }

    public static CustomerPageResponse from(CustomerPage result) {
        return new CustomerPageResponse(
                result.content().stream().map(CustomerResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
