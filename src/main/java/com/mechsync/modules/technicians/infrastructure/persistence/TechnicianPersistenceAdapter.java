package com.mechsync.modules.technicians.infrastructure.persistence;

import com.mechsync.modules.technicians.application.port.out.TechnicianRepositoryPort;
import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianDetailsProjection;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianJpaRepository;
import java.util.List;
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
                projection.getHireDate());
    }
}
