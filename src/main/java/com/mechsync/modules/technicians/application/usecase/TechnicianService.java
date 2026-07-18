package com.mechsync.modules.technicians.application.usecase;

import com.mechsync.modules.technicians.application.dto.CreateTechnicianCommand;
import com.mechsync.modules.technicians.application.dto.UpdateTechnicianCommand;
import com.mechsync.modules.technicians.application.port.in.CreateTechnicianUseCase;
import com.mechsync.modules.technicians.application.port.in.GetTechnicianByIdUseCase;
import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
import com.mechsync.modules.technicians.application.port.in.UpdateTechnicianUseCase;
import com.mechsync.modules.technicians.application.port.out.TechnicianRepositoryPort;
import com.mechsync.modules.technicians.domain.exception.DuplicateTechnicianException;
import com.mechsync.modules.technicians.domain.exception.TechnicianNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianSpecialtyNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianUserNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianUserRoleRequiredException;
import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.shared.domain.constant.SystemRole;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TechnicianService implements
        ListTechniciansUseCase,
        GetTechnicianByIdUseCase,
        CreateTechnicianUseCase,
        UpdateTechnicianUseCase {

    private final TechnicianRepositoryPort repository;

    public TechnicianService(TechnicianRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Technician> list() {
        return repository.findAll();
    }

    @Override
    public Technician getById(Long technicianId) {
        return repository.findById(technicianId)
                .orElseThrow(() -> new TechnicianNotFoundException(technicianId));
    }

    @Override
    @Transactional
    public Technician create(CreateTechnicianCommand command) {
        validateUser(command.userId());
        validateSpecialty(command.specialtyId());
        if (repository.existsByUserId(command.userId())) {
            throw new DuplicateTechnicianException(command.userId());
        }
        return repository.save(new Technician(
                null,
                command.userId(),
                null,
                null,
                null,
                null,
                command.specialtyId(),
                null,
                command.hireDate(),
                null,
                null));
    }

    @Override
    @Transactional
    public Technician update(UpdateTechnicianCommand command) {
        Technician current = getById(command.technicianId());
        validateSpecialty(command.specialtyId());
        return repository.save(new Technician(
                current.id(),
                current.userId(),
                current.firstName(),
                current.lastName(),
                current.email(),
                current.phone(),
                command.specialtyId(),
                current.specialtyCode(),
                command.hireDate(),
                current.createdAt(),
                LocalDateTime.now()));
    }

    private void validateUser(Long userId) {
        if (!repository.userExists(userId)) {
            throw new TechnicianUserNotFoundException(userId);
        }
        if (!repository.userHasRole(userId, SystemRole.TECNICO.name())) {
            throw new TechnicianUserRoleRequiredException(userId);
        }
    }

    private void validateSpecialty(Long specialtyId) {
        if (!repository.specialtyExists(specialtyId)) {
            throw new TechnicianSpecialtyNotFoundException(specialtyId);
        }
    }
}
