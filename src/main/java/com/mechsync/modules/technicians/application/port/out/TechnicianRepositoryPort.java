package com.mechsync.modules.technicians.application.port.out;

import com.mechsync.modules.technicians.domain.model.Technician;
import java.util.List;
import java.util.Optional;

public interface TechnicianRepositoryPort {

    List<Technician> findAll();

    Optional<Technician> findById(Long technicianId);

    boolean userExists(Long userId);

    boolean userHasRole(Long userId, String roleName);

    boolean existsByUserId(Long userId);

    boolean specialtyExists(Long specialtyId);

    Technician save(Technician technician);
}
