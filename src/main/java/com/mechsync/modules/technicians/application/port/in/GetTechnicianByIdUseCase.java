package com.mechsync.modules.technicians.application.port.in;

import com.mechsync.modules.technicians.domain.model.Technician;

public interface GetTechnicianByIdUseCase {

    Technician getById(Long technicianId);
}
