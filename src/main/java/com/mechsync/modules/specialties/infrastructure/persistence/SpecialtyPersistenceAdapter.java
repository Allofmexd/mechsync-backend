package com.mechsync.modules.specialties.infrastructure.persistence;

import com.mechsync.modules.specialties.application.port.out.SpecialtyRepositoryPort;
import com.mechsync.modules.specialties.domain.model.Specialty;
import com.mechsync.modules.specialties.infrastructure.repository.SpecialtyJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyPersistenceAdapter implements SpecialtyRepositoryPort {

    private final SpecialtyJpaRepository repository;

    public SpecialtyPersistenceAdapter(SpecialtyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Specialty> findAllByName() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(entity -> new Specialty(entity.getId(), entity.getName(), entity.getDescription()))
                .toList();
    }
}
