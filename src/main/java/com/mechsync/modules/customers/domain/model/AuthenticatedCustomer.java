package com.mechsync.modules.customers.domain.model;

/** Minimal identity used by customer-owned application use cases. */
public record AuthenticatedCustomer(Long customerId) {
}
