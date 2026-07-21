package com.mechsync.modules.customerportal.domain.model;

public record CustomerPortalProfile(
        Long customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address) {
}
