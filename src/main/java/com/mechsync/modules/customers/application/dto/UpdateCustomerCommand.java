package com.mechsync.modules.customers.application.dto;

public record UpdateCustomerCommand(Long customerId, String address) {
}
