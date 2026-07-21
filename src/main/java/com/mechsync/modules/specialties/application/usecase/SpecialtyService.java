package com.mechsync.modules.specialties.application.usecase;

import com.mechsync.modules.specialties.application.port.in.ListSpecialtiesUseCase;
import com.mechsync.modules.specialties.application.port.out.SpecialtyRepositoryPort;
import com.mechsync.modules.specialties.domain.model.Specialty;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SpecialtyService implements ListSpecialtiesUseCase {

    private final SpecialtyRepositoryPort repository;

    public SpecialtyService(SpecialtyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Specialty> list() {
        return repository.findAllByName();
    }
}
