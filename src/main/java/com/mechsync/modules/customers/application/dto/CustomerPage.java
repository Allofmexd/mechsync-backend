package com.mechsync.modules.customers.application.dto;

import com.mechsync.modules.customers.domain.model.Customer;
import java.util.List;

public record CustomerPage(
        List<Customer> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public CustomerPage {
        content = List.copyOf(content);
    }
}
