package com.mechsync.modules.specialties.application.port.in;

import com.mechsync.modules.specialties.domain.model.Specialty;
import java.util.List;

public interface ListSpecialtiesUseCase {
    List<Specialty> list();
}
