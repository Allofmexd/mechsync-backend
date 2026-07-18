package com.mechsync.modules.technicians.application.port.in;

import com.mechsync.modules.technicians.application.dto.CreateTechnicianCommand;
import com.mechsync.modules.technicians.domain.model.Technician;

public interface CreateTechnicianUseCase {

    Technician create(CreateTechnicianCommand command);
}
