package com.mechsync.modules.technicians.infrastructure.persistence;

import com.mechsync.modules.technicians.domain.exception.DuplicateTechnicianException;
import com.mechsync.modules.technicians.application.port.out.TechnicianRepositoryPort;
import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianDetailsProjection;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class TechnicianPersistenceAdapter implements TechnicianRepositoryPort {

    private final TechnicianJpaRepository repository;

    public TechnicianPersistenceAdapter(TechnicianJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Technician> findAll() {
        return repository.findAllDetails().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Technician> findById(Long technicianId) {
        return repository.findDetailsById(technicianId).map(this::toDomain);
    }

    @Override
    public boolean userExists(Long userId) {
        return repository.countUsersById(userId) > 0;
    }

    @Override
    public boolean userHasRole(Long userId, String roleName) {
        return repository.countUserRoles(userId, roleName) > 0;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public boolean specialtyExists(Long specialtyId) {
        return repository.countSpecialtiesById(specialtyId) > 0;
    }

    @Override
    public Technician save(Technician technician) {
        try {
            TechnicianJpaEntity saved = repository.saveAndFlush(new TechnicianJpaEntity(
                    technician.id(),
                    technician.userId(),
                    technician.specialtyId(),
                    technician.hireDate(),
                    technician.createdAt(),
                    technician.updatedAt()));
            return repository.findDetailsById(saved.getId())
                    .map(this::toDomain)
                    .orElseThrow();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateTechnicianException(technician.userId());
        }
    }

    private Technician toDomain(TechnicianDetailsProjection projection) {
        return new Technician(
                projection.getId(),
                projection.getUserId(),
                projection.getFirstName(),
                projection.getLastName(),
                projection.getEmail(),
                projection.getPhone(),
                projection.getSpecialtyId(),
                projection.getSpecialtyCode(),
                projection.getHireDate(),
                projection.getCreatedAt(),
                projection.getUpdatedAt());
    }
}
