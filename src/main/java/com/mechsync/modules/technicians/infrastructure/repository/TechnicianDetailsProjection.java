package com.mechsync.modules.technicians.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TechnicianDetailsProjection {

    Long getId();

    Long getUserId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getPhone();

    Long getSpecialtyId();

    String getSpecialtyCode();

    LocalDate getHireDate();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
