package com.mechsync.modules.technicians.application.port.out;

import com.mechsync.modules.technicians.domain.model.Technician;
import java.util.List;

public interface TechnicianRepositoryPort {

    List<Technician> findAll();
}
