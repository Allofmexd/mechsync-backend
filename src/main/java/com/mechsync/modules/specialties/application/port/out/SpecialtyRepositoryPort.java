package com.mechsync.modules.specialties.application.port.out;

import com.mechsync.modules.specialties.domain.model.Specialty;
import java.util.List;

public interface SpecialtyRepositoryPort {
    List<Specialty> findAllByName();
}
