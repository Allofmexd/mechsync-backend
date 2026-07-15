package com.mechsync.modules.technicians.application.usecase;

import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
import com.mechsync.modules.technicians.application.port.out.TechnicianRepositoryPort;
import com.mechsync.modules.technicians.domain.model.Technician;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TechnicianService implements ListTechniciansUseCase {

    private final TechnicianRepositoryPort repository;

    public TechnicianService(TechnicianRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Technician> list() {
        return repository.findAll();
    }
}
